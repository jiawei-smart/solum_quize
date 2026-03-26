import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import matplotlib.dates as mdates

file_path = "./data/bmw_global_sales_2018_2025.csv"

df = pd.read_csv(file_path)
df['Date_obj'] = pd.to_datetime(df['Year'].astype(str) + '-' + df['Month'].astype(str))
df['Date'] = pd.to_datetime(df['Date_obj'])


df['PED']= pd.to_numeric(df['Units_Sold']) * pd.to_numeric(df['Avg_Price_EUR'])

target_models = ["X7", "iX", "i4"]
df_filtered = df[df['Model'].isin(target_models)].copy()

regions = sorted(df_filtered['Region'].unique())
num_regions = len(regions)

fig, axes = plt.subplots(num_regions, 1, figsize=(15, num_regions * 5), sharex=True)

if num_regions == 1: axes = [axes]

palette = sns.color_palette("tab10", len(target_models))
model_colors = dict(zip(target_models, palette))

for i, region in enumerate(regions):
    ax1 = axes[i]
    region_data = df_filtered[df_filtered['Region'] == region]

    for model in target_models:
        model_data = region_data[region_data['Model'] == model].sort_values('Date')
        if not model_data.empty:
            ax1.plot(model_data['Date'], model_data['PED'],
                     label=f'PED: {model}', color=model_colors[model],
                     marker='o', linewidth=2, markersize=4)

    ax1.set_ylabel('PED value', fontweight='bold')
    ax1.grid(True, linestyle=':', alpha=0.6)

    ax2 = ax1.twinx()
    color_gdp = 'crimson'

    gdp_data = region_data.drop_duplicates('Date').sort_values('Date')

    ax2.plot(gdp_data['Date'], gdp_data['GDP_Growth'],
             color=color_gdp, linestyle='--', alpha=0.4, label='GDP Growth')
    ax2.fill_between(gdp_data['Date'], gdp_data['GDP_Growth'], color=color_gdp, alpha=0.05)
    ax2.set_ylabel('GDP Growth', color=color_gdp)

    ax1.set_title(f"region: {region} - model {', '.join(target_models)} ", fontsize=14)
    ax1.xaxis.set_major_formatter(mdates.DateFormatter('%Y-%m'))

    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2, loc='upper left', ncol=len(target_models) + 1)

plt.tight_layout()
plt.show()