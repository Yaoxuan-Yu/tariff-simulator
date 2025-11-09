import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ShoppingCart } from "lucide-react"
import { useState } from "react"
import { Alert, AlertDescription } from "@/components/ui/alert"

interface ResultsTableProps {
  results: any
  onAddToCart: (calculation: any) => boolean | Promise<boolean>
}

export function ResultsTable({ results, onAddToCart }: ResultsTableProps) {
  const [exportError, setExportError] = useState("")
  const [successMessage, setSuccessMessage] = useState("")
  const [debugInfo, setDebugInfo] = useState("")

  const handleAddingExport = async () => {
    try {
      setExportError("")
      setSuccessMessage("")
      setDebugInfo("")

      if (!results) {
        setExportError("No calculation data available")
        return
      }

      const data = results.data || results
      const calculationId = data.calculationId
      
      console.log("🔍 ResultsTable - Full results object:", results)
      console.log("🔍 ResultsTable - Extracted data:", data)
      console.log("🔍 ResultsTable - Calculation ID:", calculationId)

      if (!calculationId) {
        setExportError("Calculation ID not found. Please calculate again.")
        return
      }

      // Import supabase to get auth token
      const supabase = (await import("@/lib/supabaseClient")).default
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token

      // Call backend API with calculationId in path
      const response = await fetch(`http://localhost:8080/api/export-cart/add/${encodeURIComponent(calculationId)}`, {
        method: 'POST',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json',
        },
        credentials: 'include'
      })

      if (!response.ok) {
        const responseText = await response.text()
        let errorMessage = `Server returned ${response.status}`
        try {
          const errorData = JSON.parse(responseText)
          errorMessage = errorData.message || errorData.error || errorMessage
        } catch {
          errorMessage = responseText || errorMessage
        }
        
        if (response.status === 400 && errorMessage.includes("already in cart")) {
          setExportError("This calculation is already in your export cart")
        } else if (response.status === 404) {
          setExportError("Calculation not found in history. Please calculate again.")
        } else {
          setExportError(`Failed to add to cart: ${errorMessage}`)
        }
        return
      }

      setSuccessMessage("Calculation added to export cart successfully!")
      setTimeout(() => setSuccessMessage(""), 3000)
      
      // Trigger callback to refresh cart count in parent
      await onAddToCart(results)

    } catch (error: any) {
      console.error("Error adding to cart:", error)
      setExportError(`Failed to add to cart: ${error.message}`)
    }
  }

  const data = results?.data || results
  
  if (!data || !data.breakdown) {
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
          onClick={handleAddingExport}
          variant="outline"
          size="sm"
          className="flex items-center gap-2 bg-transparent"
        >
          <ShoppingCart className="h-4 w-4" />
          Add To Export Cart
        </Button>
      </CardHeader>
      <CardContent>
        {exportError && (
          <Alert variant="destructive" className="mb-4">
            <AlertDescription>{exportError}</AlertDescription>
          </Alert>
        )}

        {successMessage && (
          <Alert className="mb-4">
            <AlertDescription>{successMessage}</AlertDescription>
          </Alert>
        )}

        {debugInfo && (
          <Alert className="mb-4 bg-yellow-50 border-yellow-200">
            <AlertDescription>
              <details>
                <summary className="cursor-pointer font-semibold">Debug Information</summary>
                <pre className="mt-2 text-xs overflow-auto max-h-40">{debugInfo}</pre>
              </details>
            </AlertDescription>
          </Alert>
        )}

        <div className="space-y-6">
          <div className="bg-muted p-4 rounded-lg">
            <h3 className="font-semibold text-foreground mb-2">Summary</h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Product</p>
                <p className="font-medium">{data.product}</p>
                <p className="text-xs text-muted-foreground">Brand: {data.brand}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Route</p>
                <p className="font-medium">
                  {data.exportingFrom} → {data.importingTo}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground">Quantity</p>
                <p className="font-medium">
                  {data.quantity} x {data.unit}
                </p>
                <p className="text-xs text-muted-foreground">Total Product Cost: ${data.productCost.toFixed(2)}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Total Cost</p>
                <p className="font-bold text-foreground">${data.totalCost.toFixed(2)}</p>
                <p className="text-xs text-muted-foreground">{data.tariffType}</p>
              </div>
            </div>
          </div>

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
                {data.breakdown.map((item: any, index: number) => (
                  <tr key={index} className="border-b border-border hover:bg-muted/50">
                    <td className="py-3 px-4 text-foreground">{item.description}</td>
                    <td className="py-3 px-4">
                      <Badge
                        variant="secondary"
                        className={
                          item.type?.toLowerCase() === "tariff"
                            ? "bg-accent text-accent-foreground"
                            : item.type?.toLowerCase() === "ahs"
                            ? "bg-primary text-primary-foreground"
                            : ""
                        }
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
                  <td className="py-3 px-4 text-right font-bold text-foreground">${data.totalCost.toFixed(2)}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
