"use client"
import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { exportToCSV } from "@/lib/csv-export"
import { Download, Plus } from "lucide-react"

interface TariffDefinition {
  id: string
  product: string
  exportingFrom: string
  importingTo: string
  type: string
  rate: number
  effectiveDate: string
  expirationDate: string
}

interface TariffDefinitionsResponse {
  success: boolean
  data?: TariffDefinition[]
  error?: string
}

export function TariffDefinitionsTable() {
  const [tariffs, setTariffs] = useState<TariffDefinition[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState("")
  
  // Only show tariffs with a non-zero rate
  const visibleTariffs = tariffs.filter((t) => Number(t.rate) !== 0)
  
  useEffect(() => {
    const loadTariffs = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/tariff-definitions')
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const data: TariffDefinitionsResponse = await response.json()
        
        if (data.success && data.data) {
          setTariffs(data.data)
        } else {
          setError(data.error || "Failed to load tariff definitions")
        }
      } catch (err) {
        console.error('Failed to fetch tariff definitions from backend:', err)
        setError("An unexpected error occurred")
      } finally {
        setIsLoading(false)
      }
    }
    loadTariffs()
  }, [])
  const handleExportCSV = () => {
    try {
      if (!visibleTariffs || visibleTariffs.length === 0) return
      
      const csvData = visibleTariffs.map((tariff) => ({
        Product: tariff.product,
        "Exporting From": tariff.exportingFrom,
        "Importing To": tariff.importingTo,
        Type: tariff.type,
        Rate: `${tariff.rate}%`,
        "Effective Date": tariff.effectiveDate,
        "Expiration Date": tariff.expirationDate,
      }))
      exportToCSV(csvData, "tariff-definitions")
    } catch (err) {
      setError("Failed to export CSV file")
    }
  }
  if (isLoading) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center h-64">
          <p className="text-muted-foreground">Loading tariff definitions...</p>
        </CardContent>
      </Card>
    )
  }
  if (error) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center h-64">
          <p className="text-destructive">{error}</p>
        </CardContent>
      </Card>
    )
  }
  return (
    <Card>
      <CardHeader>
        <div className="flex justify-between items-start">
          <div>
            <CardTitle className="text-xl font-semibold text-foreground">Current Tariffs</CardTitle>
            <CardDescription>A list of all defined tariffs and fees in the system.</CardDescription>
          </div>
          <div className="flex space-x-2">
            <Button onClick={handleExportCSV} variant="outline" size="sm">
              <Download className="h-4 w-4 mr-2" />
              Export CSV
            </Button>
            <Button className="bg-accent hover:bg-accent/90 text-accent-foreground" size="sm">
              <Plus className="h-4 w-4 mr-2" />
              Define New Tariff
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <table className="w-full border-collapse">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left py-3 px-4 font-medium text-muted-foreground">Product</th>
                <th className="text-left py-3 px-4 font-medium text-muted-foreground">Exporting From</th>
                <th className="text-left py-3 px-4 font-medium text-muted-foreground">Importing To</th>
                <th className="text-left py-3 px-4 font-medium text-muted-foreground">Type</th>
                <th className="text-left py-3 px-4 font-medium text-muted-foreground">Rate</th>
                <th className="text-left py-3 px-4 font-medium text-muted-foreground">Effective Date</th>
                <th className="text-left py-3 px-4 font-medium text-muted-foreground">Expiration Date</th>
                <th className="text-left py-3 px-4 font-medium text-muted-foreground">Actions</th>
              </tr>
            </thead>
            <tbody>
              {visibleTariffs.length === 0 && (
                <tr>
                  <td className="py-6 px-4 text-muted-foreground" colSpan={8}>No tariffs with a rate above 0%.</td>
                </tr>
              )}
              {visibleTariffs.map((tariff) => (
                <tr key={tariff.id} className="border-b border-border hover:bg-muted/50">
                  <td className="py-3 px-4 text-foreground font-medium">{tariff.product}</td>
                  <td className="py-3 px-4 text-muted-foreground">{tariff.exportingFrom}</td>
                  <td className="py-3 px-4 text-muted-foreground">{tariff.importingTo}</td>
                  <td className="py-3 px-4">
                    <Badge
                      variant={tariff.type === "AHS" ? "default" : "secondary"}
                      className={tariff.type === "AHS" ? "bg-accent/20 text-accent-foreground" : ""}
                    >
                      {tariff.type}
                    </Badge>
                  </td>
                  <td className="py-3 px-4 text-foreground">{tariff.rate}%</td>
                  <td className="py-3 px-4 text-muted-foreground">{tariff.effectiveDate}</td>
                  <td className="py-3 px-4 text-muted-foreground">{tariff.expirationDate}</td>
                  <td className="py-3 px-4">
                    <Button variant="ghost" size="sm" className="text-muted-foreground hover:text-foreground">
                      •••
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  )
}
