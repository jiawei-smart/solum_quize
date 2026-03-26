

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import math
import matplotlib.dates as mdates

file_path = "./data/bmw_global_sales_2018_2025.csv"
df = pd.read_csv(file_path)

df['Date_obj'] = pd.to_datetime(df['Year'].astype(str) + '-' + df['Month'].astype(str))
df['Date'] = pd.to_datetime(df['Date_obj'])

df['PED']= pd.to_numeric(df['Units_Sold']) * pd.to_numeric(df['Avg_Price_EUR'])

df['ped_smooth'] = df.groupby(['Region', 'Model'])['PED'].transform(
    lambda x: x.rolling(window=3, min_periods=1).mean()
)

models = df['Model'].unique()

regions = df['Region'].unique()
num_regions = len(regions)

cols = 2
rows = math.ceil(num_regions / cols)

fig, axes = plt.subplots(rows, cols, figsize=(15, rows * 5), sharex=True)
axes = axes.flatten()

palette = sns.color_palette("tab10", len(models))
color_map = dict(zip(models, palette))

for i, region in enumerate(regions):

    ax = axes[i]
    region_data = df[df['Region'] == region].sort_values('Date')

    sns.lineplot(data=region_data,
                 x='Date',
                 y='ped_smooth',
                 hue='Model',
                 palette=color_map,
                 linewidth=1.5,
                 ax=ax)

    ax.set_title(f"Region: {region} (3 months PED)", fontsize=14, fontweight='bold')
    ax.set_ylabel("PED ", fontsize=11)
    ax.grid(True, linestyle=':', alpha=0.5)

    ax.xaxis.set_major_formatter(mdates.DateFormatter('%Y'))
    ax.xaxis.set_major_locator(mdates.YearLocator())
    plt.setp(ax.get_xticklabels(), rotation=0, ha='center')

    ax.get_legend().remove()

for j in range(i + 1, len(axes)):
    fig.delaxes(axes[j])

handles, labels = axes[0].get_legend_handles_labels()
fig.legend(handles, labels, loc='upper right', bbox_to_anchor=(0.98, 0.95),
           title='Model', title_fontsize='12', fontsize='11', frameon=True)

plt.tight_layout(rect=[0, 0, 0.88, 1])
plt.show()

