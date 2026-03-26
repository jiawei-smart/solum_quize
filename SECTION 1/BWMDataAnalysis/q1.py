# load cvs file into memory

import pandas as pd
import matplotlib.pyplot as plt
from sklearn.preprocessing import MinMaxScaler
import math

file_path = "./data/bmw_global_sales_2018_2025.csv"
df = pd.read_csv(file_path)

df['BEV_unit']= pd.to_numeric(df['Units_Sold']) * pd.to_numeric(df['BEV_Share'])

key_columns = df.columns[:3].tolist()
df = df.groupby(key_columns, as_index=False).sum()

df['BEV_Share_advised']=  ( pd.to_numeric(df['BEV_unit']) / pd.to_numeric(df['Units_Sold']) ).round(3)

df['Date_obj'] = pd.to_datetime(df['Year'].astype(str) + '-' + df['Month'].astype(str))
df['Date'] = pd.to_datetime(df['Date_obj'])

metrics = ['Units_Sold', 'Revenue_EUR', 'BEV_Share_advised']

regions = df['Region'].unique()
num_regions = len(regions)

cols = 2
rows = math.ceil(num_regions / cols)

fig, axes = plt.subplots(rows, cols, figsize=(15, rows * 5), sharex=True)
axes = axes.flatten()
scaler = MinMaxScaler()

for i, region in enumerate(regions):
    ax = axes[i]
    region_data = df[df['Region'] == region].sort_values('Date')
    region_data[metrics] = scaler.fit_transform(region_data[metrics])


    for metric in metrics:
        ax.plot(region_data['Date'], region_data[metric], marker='o', label=metric, linewidth=2)


        ax.set_title(f"{region} ", fontsize=13, fontweight='bold')
        ax.grid(True, linestyle=':', alpha=0.6)

        plt.setp(ax.get_xticklabels(), rotation=30, ha='right')

        if i == 0:
            ax.legend(loc='upper left', fontsize='small')


for j in range(i + 1, len(axes)):
    fig.delaxes(axes[j])

plt.tight_layout()
plt.show()