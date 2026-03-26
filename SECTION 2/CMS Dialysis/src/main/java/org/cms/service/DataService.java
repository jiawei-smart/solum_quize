package org.cms.service;

import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cms.entity.MortalityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Setter
@ConfigurationProperties(prefix = "remote.api")
public class DataService {
    private static final Logger log = LoggerFactory.getLogger(DataService.class);
    private static final String TABLE_NAME = "mortality_data";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final JdbcTemplate jdbcTemplate;

    private List<Map<String, Object>> cachedData = new ArrayList<>();
    public DataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initDynamicDatabase() {
        try {
            ClassPathResource resource = new ClassPathResource("Complications_and_Deaths-Hospital.csv");

            try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String [] headers = {"facilityId", "facilityName", "address", "cityTown", "state", "zipCode", "countyParish", "telephoneNumber", "measureId", "measureName", "comparedToNational", "denominator", "score", "lowerEstimate", "higherEstimate", "footnote", "startDate", "endDate"};
                String columns = Arrays.stream(headers)
                        .map(header -> "\"" + header.trim() + "\" TEXT")
                        .collect(Collectors.joining(", "));

                String createTableSql = String.format("CREATE TABLE %s (%s)", TABLE_NAME, columns);

                jdbcTemplate.execute("DROP TABLE IF EXISTS " + TABLE_NAME);
                jdbcTemplate.execute(createTableSql);


                String placeholders = Arrays.stream(headers).map(h -> "?").collect(Collectors.joining(", "));
                String insertSql = String.format("INSERT INTO %s VALUES (%s)", TABLE_NAME, placeholders);

                String[] nextLine;
                int count = 0;
                reader.readNext(); // skip header
                while ((nextLine = reader.readNext()) != null) {
                    jdbcTemplate.update(insertSql, (Object[]) nextLine);
                    count++;
                }

                log.info("Created data table: import {} row data", count);
            }
        } catch (Exception e) {
            log.error("init data table failure with: {}", e.getMessage());
        }
    }

    public Map<String, Object> calculateSummary(String measureId, String year, String month, String state , String zipCode, String facilityName, int page, int pageSize) {
        int offset = (page - 1) * pageSize;

        StringBuilder dateTableSQL = new StringBuilder("""
            SELECT 
               *
            FROM mortality_data 
            WHERE measureId = ?
            """);
        StringBuilder countSQL = new StringBuilder("""
            SELECT 
                COUNT(*)
            FROM mortality_data 
            WHERE  measureId = ?
            """);

        StringBuilder maxSQL = new StringBuilder("""
            SELECT MAX(CAST(Score AS REAL)) 
            FROM mortality_data 
            WHERE  measureId = ?
        """);
        StringBuilder minSQL = new StringBuilder("""
            SELECT MIN(CAST(Score AS REAL)) 
            FROM mortality_data 
            WHERE  measureId = ?
        """);

        StringBuilder averageSQL = new StringBuilder("""
            SELECT AVG(CAST(Score AS REAL)) 
            FROM mortality_data 
            WHERE  measureId = ?
        """);

        StringBuilder top10HighestSQL = new StringBuilder("""
            SELECT facilityName
            FROM mortality_data 
            WHERE  measureId = ?
        """);
        enrichSqlWithFilters(top10HighestSQL, year, month, state, zipCode, facilityName);
        top10HighestSQL.append(" ORDER BY CAST(Score AS REAL) DESC LIMIT 10");

        StringBuilder top10LowestSQL = new StringBuilder("""
            SELECT facilityName
            FROM mortality_data 
            WHERE measureId = ?
            
        """);
        enrichSqlWithFilters(top10LowestSQL, year, month, state, zipCode, facilityName);
        top10LowestSQL.append(" ORDER BY CAST(Score AS REAL) ASC LIMIT 10");

        enrichSqlWithFilters(dateTableSQL, year, month, state, zipCode, facilityName);
        enrichSqlWithFilters(countSQL, year, month, state, zipCode, facilityName);
        enrichSqlWithFilters(maxSQL, year, month, state, zipCode, facilityName);
        enrichSqlWithFilters(minSQL, year, month, state, zipCode, facilityName);
        enrichSqlWithFilters(averageSQL, year, month, state, zipCode, facilityName);

        dateTableSQL.append(" LIMIT "+pageSize+" OFFSET "+offset);

        List<Object> params = new ArrayList<>(Collections.singletonList(measureId));

        if (StringUtils.isNoneEmpty(state)) {
            params.add(state);
        }
        if (StringUtils.isNoneEmpty(zipCode)) {
            params.add(zipCode);
        }
        if (StringUtils.isNoneEmpty(facilityName)) {
            params.add("%" + facilityName + "%");
        }
        if (StringUtils.isNoneEmpty(year)) {
            if(StringUtils.isEmpty(month)) {
                month = "12";
            }
            params.add(year+month);
        }
        Map<String, Object> result = new HashMap<>();

        List<MortalityData> tableData = jdbcTemplate.query(dateTableSQL.toString(), new BeanPropertyRowMapper<>(MortalityData.class),params.toArray());
        result.put("tableData", tableData);
        result.put("averageRate", jdbcTemplate.queryForObject(averageSQL.toString(), params.toArray(), Double.class));
        result.put("max", jdbcTemplate.queryForObject(maxSQL.toString(), params.toArray(), Double.class));
        result.put("min", jdbcTemplate.queryForObject(minSQL.toString(), params.toArray(), Double.class));
        result.put("top10Highest", jdbcTemplate.queryForList(top10HighestSQL.toString(), String.class, params.toArray()));
        result.put("top10Lowest", jdbcTemplate.queryForList(top10LowestSQL.toString(), String.class, params.toArray()));
        result.put("total", jdbcTemplate.queryForObject(countSQL.toString(), params.toArray(), Integer.class));

        log.info("Calculated summary with filters - year: {}, month: {}, state: {}, zipCode: {}, facilityName: {}, page: {}, pageSize: {}", year, month, state, zipCode, facilityName, page, pageSize);

        return result;
    }

    private void enrichSqlWithFilters(StringBuilder sql, String year, String month, String state, String zipCode, String facilityName) {
        if(StringUtils.isNoneEmpty(state)) {
            sql.append(" AND \"state\" = ? ");
        }
        if(StringUtils.isNoneEmpty(zipCode)) {
            sql.append(" AND \"zipCode\" = ? ");
        }
        if(StringUtils.isNoneEmpty(facilityName)) {
            sql.append(" AND \"facilityName\" LIKE ? ");
        }
        if(StringUtils.isNoneEmpty(year)) {
            sql.append("  AND substr(endDate, 7, 4) || substr(endDate, 1, 2) <= ? ");
        }
    }

    public List<String> getAllMeasureIds() {
        return jdbcTemplate.queryForList("SELECT DISTINCT measureId FROM mortality_data", String.class);
    }

    public List<String> getAllFacilities() {
        return jdbcTemplate.queryForList("SELECT DISTINCT facilityName FROM mortality_data", String.class);
    }

    public List<String> getAllStates() {
        return jdbcTemplate.queryForList("SELECT DISTINCT state FROM mortality_data", String.class);
    }

    public List<String> getAllZipCodes() {
        return jdbcTemplate.queryForList("SELECT DISTINCT zipCode FROM mortality_data", String.class);
    }

    public List<MortalityData> getAnalysisData(String measureId, String facilityId, String state, String zipCode) {
        StringBuilder sql = new StringBuilder("""
                SELECT * FROM mortality_data WHERE measureId = ? AND Score != 'Not Available' 
                """);

        List<Object> params = new ArrayList<>(Collections.singletonList(measureId));

        if (StringUtils.isNoneEmpty(facilityId)) {
            sql.append(" AND facilityName  = ? ");
            params.add(facilityId);
        }

        if (StringUtils.isNoneEmpty(state)) {
            sql.append(" AND state = ?");
            params.add(state);
        }
        if (StringUtils.isNoneEmpty(zipCode)) {
            sql.append(" AND zipCode = ?");
            params.add(zipCode);
        }

        log.info("Getting analysis data with filters - measureId: {}, facilityId: {}, state: {}, zipCode: {}", measureId, facilityId, state, zipCode);
        log.info("Constructed SQL: {}, with params: {}", sql, params);

        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(MortalityData.class), params.toArray());
    }
}
