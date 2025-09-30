"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Download } from "lucide-react"
import { useState } from "react"
import { Alert, AlertDescription } from "@/components/ui/alert"

interface ResultsTableProps {
  results: any
}

export function ResultsTable({ results }: ResultsTableProps) {
  const [exportError, setExportError] = useState("")

  const handleExportCSV = async () => {
    try {
      setExportError("")
      // TODO: Replace with actual backend API call for CSV export
      console.log("Export CSV requested for results:", results)
      alert("CSV export functionality will be handled by the backend")
    } catch (error) {
      setExportError("Failed to export CSV. Please try again.")
    }
  }

  if (!results || !results.breakdown) {
    return null
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <div>
          <CardTitle className="text-xl font-semibold text-foreground">Calculation Results</CardTitle>
          <CardDescription>Detailed breakdown of import costs and tariffs</CardDescription>
        </div>
        <Button
          onClick={handleExportCSV}
          variant="outline"
          size="sm"
          className="flex items-center gap-2 bg-transparent"
        >
          <Download className="h-4 w-4" />
          Export CSV
        </Button>
      </CardHeader>
      <CardContent>
        {exportError && (
          <Alert variant="destructive" className="mb-4">
            <AlertDescription>{exportError}</AlertDescription>
          </Alert>
        )}

        <div className="space-y-6">
          {/* Summary */}
          <div className="bg-muted p-4 rounded-lg">
            <h3 className="font-semibold text-foreground mb-2">Summary</h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Product</p>
                <p className="font-medium">{results.product}</p>
                <p className="text-xs text-muted-foreground">{results.brand}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Route</p>
                <p className="font-medium">
                  {results.exportingFrom} → {results.importingTo}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground">Quantity</p>
                <p className="font-medium">
                  {results.quantity} {results.unit}
                </p>
                <p className="text-xs text-muted-foreground">Product Cost: ${results.productCost.toFixed(2)}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Total Cost</p>
                <p className="font-bold text-accent">${results.totalCost.toFixed(2)}</p>
                <p className="text-xs text-muted-foreground">{results.tariffType}</p>
              </div>
            </div>
          </div>

          {/* Breakdown Table */}
          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-3 px-4 font-semibold text-foreground">Description</th>
                  <th className="text-left py-3 px-4 font-semibold text-foreground">Type</th>
                  <th className="text-right py-3 px-4 font-semibold text-foreground">Rate</th>
                  <th className="text-right py-3 px-4 font-semibold text-foreground">Amount</th>
                </tr>
              </thead>
              <tbody>
                {results.breakdown.map((item: any, index: number) => (
                  <tr key={index} className="border-b border-border hover:bg-muted/50">
                    <td className="py-3 px-4 text-foreground">{item.description}</td>
                    <td className="py-3 px-4">
                      <Badge
                        variant={item.type === "Tariff" ? "default" : "secondary"}
                        className={item.type === "Tariff" ? "bg-accent/20 text-accent-foreground" : ""}
                      >
                        {item.type}
                      </Badge>
                    </td>
                    <td className="py-3 px-4 text-right text-muted-foreground">{item.rate}</td>
                    <td className="py-3 px-4 text-right font-medium text-foreground">${item.amount.toFixed(2)}</td>
                  </tr>
                ))}
                <tr className="border-t-2 border-border bg-muted/50">
                  <td className="py-3 px-4 font-bold text-foreground" colSpan={3}>
                    Total Cost
                  </td>
                  <td className="py-3 px-4 text-right font-bold text-accent">${results.totalCost.toFixed(2)}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
