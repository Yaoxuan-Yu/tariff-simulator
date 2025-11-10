"use client"

import { useMemo } from "react"
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts"
import type { TariffSeries } from "@/components/tariff-trends-visualization"
import { Skeleton } from "@/components/ui/skeleton"

interface TariffChartProps {
  data: TariffSeries[]
  loading: boolean
  dateRange: { start: string; end: string }
}

export function TariffChart({ data, loading, dateRange }: TariffChartProps) {
  const chartData = useMemo(() => {
    if (!Array.isArray(data) || data.length === 0) return []

    const allDates = new Set<string>()
    data.forEach((series) => {
      if (Array.isArray(series.data)) {
        series.data.forEach((point) => {
          if (point && point.date) {
            allDates.add(point.date)
          }
        })
      }
    })

    const sortedDates = Array.from(allDates).sort()

    return sortedDates.map((date) => {
      const point: Record<string, any> = { date }
      data.forEach((series) => {
        if (Array.isArray(series.data)) {
          const dataPoint = series.data.find((d) => d && d.date === date)
          point[series.id] = dataPoint && typeof dataPoint.tariffRate === 'number' ? dataPoint.tariffRate : null
        }
      })
      return point
    })
  }, [data])

  if (loading) {
    return (
      <div className="w-full h-[500px] flex items-center justify-center">
        <div className="space-y-4 w-full">
          <Skeleton className="h-8 w-full" />
          <Skeleton className="h-8 w-full" />
          <Skeleton className="h-8 w-full" />
          <Skeleton className="h-8 w-full" />
          <Skeleton className="h-8 w-full" />
        </div>
      </div>
    )
  }

  if (!Array.isArray(data) || data.length === 0) {
    return (
      <div className="w-full h-[500px] flex items-center justify-center">
        <div className="text-center text-muted-foreground">
          <p className="text-lg font-medium">No data to display</p>
          <p className="text-sm">Select import country, export country, and product to view tariff trends</p>
        </div>
      </div>
    )
  }

  if (chartData.length === 0) {
    return (
      <div className="w-full h-[500px] flex items-center justify-center">
        <div className="text-center text-muted-foreground">
          <p className="text-lg font-medium">No historical data available</p>
          <p className="text-sm">There is no tariff data for the selected date range</p>
        </div>
      </div>
    )
  }

  return (
    <div className="w-full">
      <ResponsiveContainer width="100%" height={500}>
        <LineChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
          <XAxis
            dataKey="date"
            label={{ value: "Date", position: "insideBottom", offset: -5 }}
            tick={{ fontSize: 12 }}
            tickFormatter={(value) => {
              try {
                const date = new Date(value)
                if (isNaN(date.getTime())) return value
                return date.toLocaleDateString("en-US", { month: "short", year: "numeric" })
              } catch {
                return value
              }
            }}
          />
          <YAxis 
            label={{ value: "Tariff Rate (%)", angle: -90, position: "insideLeft" }} 
            tick={{ fontSize: 12 }}
            domain={[0, 'auto']}
          />
          <Tooltip content={<CustomTooltip seriesData={data} />} />
          <Legend wrapperStyle={{ paddingTop: "20px" }} />
          {data.map((series) => (
            <Line
              key={series.id}
              type="monotone"
              dataKey={series.id}
              name={`${series.importCountry} ← ${series.exportCountry}: ${series.product}`}
              stroke={series.color}
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 6 }}
              connectNulls
            />
          ))}
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}

function CustomTooltip({ active, payload, label, seriesData }: any) {
  if (!active || !payload || payload.length === 0) {
    return null
  }

  return (
    <div className="bg-popover border border-border rounded-lg shadow-lg p-3 max-w-sm">
      <p className="font-medium text-sm mb-2">
        {(() => {
          try {
            const date = new Date(label)
            if (isNaN(date.getTime())) return label
            return date.toLocaleDateString("en-US", {
              year: "numeric",
              month: "long",
              day: "numeric",
            })
          } catch {
            return label
          }
        })()}
      </p>
      <div className="space-y-2">
        {payload.map((entry: any, index: number) => {
          if (entry.value === null || entry.value === undefined) return null

          const series = seriesData.find((s: TariffSeries) => s.id === entry.dataKey)

          if (!series) return null

          return (
            <div key={index} className="space-y-1">
              <div className="flex items-center gap-2">
                <div 
                  className="w-3 h-3 rounded-full flex-shrink-0" 
                  style={{ backgroundColor: entry.color }} 
                />
                <div className="text-xs">
                  <div className="font-medium">
                    {series.importCountry} ← {series.exportCountry}
                  </div>
                  <div className="text-muted-foreground">{series.product}</div>
                </div>
              </div>
              <div className="text-sm font-semibold ml-5">
                Tariff Rate: {typeof entry.value === 'number' ? entry.value.toFixed(2) : '0.00'}%
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}