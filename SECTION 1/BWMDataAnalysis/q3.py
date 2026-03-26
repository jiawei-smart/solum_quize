import pandas as pd
import matplotlib.pyplot as plt
from matplotlib import ticker
import math

target_year = 2024

file_path = "./data/bmw_global_sales_2018_2025.csv"
df = pd.read_csv(file_path)

df['Date_obj'] = pd.to_datetime(df['Year'].astype(str) + '-' + df['Month'].astype(str))
df['date'] = pd.to_datetime(df['Date_obj'])

df_plot = df[df['Year'] == target_year].groupby(['Region', 'Month'], as_index=False).agg({
    'Revenue_EUR': 'sum',
    'Units_Sold': 'sum',
    'GDP_Growth': 'mean',
    'Fuel_Price_Index': 'mean'
})
regions = sorted(df_plot['Region'].unique())
rows = math.ceil(len(regions) / 2)
fig, axes = plt.subplots(rows, 2, figsize=(20, rows * 7), dpi=100)
axes = axes.flatten()

for i, region in enumerate(regions):
    ax = axes[i]
    reg_data = df_plot[df_plot['Region'] == region].sort_values('Month')

    for col in ['Revenue_EUR', 'Units_Sold', 'GDP_Growth', 'Fuel_Price_Index']:
        v_min, v_max = reg_data[col].min(), reg_data[col].max()
        reg_data[f'{col}_norm'] = (reg_data[col] - v_min) / (v_max - v_min) if v_max != v_min else 0.5

    ln1, = ax.plot(reg_data['Month'], reg_data['Revenue_EUR_norm'],
                   color='#1f77b4', linewidth=3.5, label='Revenue Trend', alpha=0.9)
    ln2, = ax.plot(reg_data['Month'], reg_data['Units_Sold_norm'],
                   color='#aec7e8', linestyle='--', linewidth=2.5, label='Units Trend')

    ax.fill_between(reg_data['Month'], reg_data['Revenue_EUR_norm'], color='#1f77b4', alpha=0.05)

    ax2 = ax.twinx()
    ln3, = ax2.plot(reg_data['Month'], reg_data['GDP_Growth_norm'],
                    color='#d62728', linestyle='--', linewidth=2, label='GDP Growth', alpha=0.7)
    ln4, = ax2.plot(reg_data['Month'], reg_data['Fuel_Price_Index_norm'],
                    color='#ff7f0e', linestyle=':', linewidth=2, label='Fuel Index', alpha=0.7)

    ax.set_title(f"Region: {region} | {target_year} Seasonal Interaction", fontsize=16, pad=20, fontweight='bold')
    ax.set_xticks(range(1, 13))
    ax.xaxis.set_major_locator(ticker.MultipleLocator(1))

    ax.spines['top'].set_visible(False)
    ax2.spines['top'].set_visible(False)
    ax.grid(axis='y', linestyle='--', alpha=0.3)

    if i == 0:
        lines = [ln1, ln2, ln3, ln4]
        ax.legend(lines, [l.get_label() for l in lines], loc='upper left', ncol=2, frameon=False)

plt.tight_layout(pad=5.0)
plt.show()