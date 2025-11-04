"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Checkbox } from "@/components/ui/checkbox"
import { Download, Trash2 } from "lucide-react"
import { useState, useEffect } from "react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"

interface ExportCartItem {
  calculationId: string
  product: string
  brand: string
  exportingFrom: string
  importingTo: string
  quantity: number
  unit: string
  productCost: number
  totalCost: number
  tariffType: string
  calculationDate: string
  breakdown: Array<{
    description: string
    type: string
    rate: string
    amount: number
  }>
}

export function ExportPage() {
  const [cartItems, setCartItems] = useState<ExportCartItem[]>([])
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set())
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState("")
  const [successMessage, setSuccessMessage] = useState("")
  const [showDownloadDialog, setShowDownloadDialog] = useState(false)

  useEffect(() => {
    loadCartItems()
  }, [])

  const loadCartItems = async () => {
    try {
      setIsLoading(true)
      const supabase = (await import("@/lib/supabaseClient")).default
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token
      
      const response = await fetch('http://localhost:8080/api/export-cart', {
        method: 'GET',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      })

      if (response.status === 204) {
        setCartItems([])
        return
      }

      if (!response.ok) {
        throw new Error("Failed to load export cart")
      }

      const result = await response.json()
      setCartItems(result || [])
    } catch (err) {
      console.error("Error loading cart:", err)
      setError("An unexpected error occurred while loading cart items")
    } finally {
      setIsLoading(false)
    }
  }

  const handleSelectItem = (calculationId: string) => {
    const newSelected = new Set(selectedItems)
    if (newSelected.has(calculationId)) {
      newSelected.delete(calculationId)
    } else {
      newSelected.add(calculationId)
    }
    setSelectedItems(newSelected)
  }

  const handleSelectAll = () => {
    if (selectedItems.size === cartItems.length) {
      setSelectedItems(new Set())
    } else {
      setSelectedItems(new Set(cartItems.map(item => item.calculationId)))
    }
  }

  const handleDeleteSelected = async () => {
    if (selectedItems.size === 0) {
      setError("Please select at least one item to delete")
      setTimeout(() => setError(""), 3000)
      return
    }

    if (!confirm(`Are you sure you want to delete ${selectedItems.size} selected item${selectedItems.size !== 1 ? 's' : ''}?`)) {
      return
    }

    try {
      setError("")
      let successCount = 0
      let failCount = 0

      const supabase = (await import("@/lib/supabaseClient")).default
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token

      for (const calculationId of selectedItems) {
        try {
          const response = await fetch(`http://localhost:8080/api/export-cart/remove/${calculationId}`, {
            method: 'DELETE',
            headers: {
              'Authorization': token ? `Bearer ${token}` : '',
              'Content-Type': 'application/json'
            },
            credentials: 'include'
          })

          if (response.ok) {
            successCount++
          } else {
            failCount++
          }
        } catch (err) {
          failCount++
          console.error(`Error deleting ${calculationId}:`, err)
        }
      }

      if (successCount > 0) {
        setCartItems(cartItems.filter(item => !selectedItems.has(item.calculationId)))
        setSelectedItems(new Set())
        setSuccessMessage(`Successfully deleted ${successCount} item${successCount !== 1 ? 's' : ''}`)
        setTimeout(() => setSuccessMessage(""), 3000)
      }

      if (failCount > 0) {
        setError(`Failed to delete ${failCount} item${failCount !== 1 ? 's' : ''}`)
      }
    } catch (err) {
      console.error("Error deleting items:", err)
      setError("An unexpected error occurred while deleting items")
    }
  }

  const handleClearAll = async () => {
    if (!confirm("Are you sure you want to clear all items from the cart? This action cannot be undone.")) {
      return
    }

    try {
      setError("")
      const supabase = (await import("@/lib/supabaseClient")).default
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token
      
      const response = await fetch('http://localhost:8080/api/export-cart/clear', {
        method: 'DELETE',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      })

      if (!response.ok) {
        throw new Error("Failed to clear cart")
      }

      setCartItems([])
      setSelectedItems(new Set())
      setSuccessMessage("Export cart cleared successfully")
      setTimeout(() => setSuccessMessage(""), 3000)
    } catch (err) {
      console.error("Error clearing cart:", err)
      setError("An unexpected error occurred while clearing cart")
    }
  }

  const handleDownloadClick = () => {
    if (cartItems.length === 0) {
      setError("Export cart is empty. Please add calculations to the cart first.")
      setTimeout(() => setError(""), 3000)
      return
    }

    if (selectedItems.size === 0) {
      setShowDownloadDialog(true)
    } else {
      // Note: Backend currently exports all items, not just selected ones
      // The selectedItems check is for future enhancement
      handleDownloadCSV()
    }
  }

  const handleDownloadCSV = async () => {
    try {
      setError("")
      setShowDownloadDialog(false)

      if (cartItems.length === 0) {
        setError("Export cart is empty. Please add calculations to the cart first.")
        return
      }

      // Import supabase to get auth token
      const supabase = (await import("@/lib/supabaseClient")).default
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token

      const response = await fetch('http://localhost:8080/api/export-cart/export', {
        method: 'GET',
        headers: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        credentials: 'include'
      })

      if (!response.ok) {
        let errorMessage = `Failed to download CSV (${response.status})`
        try {
          const errorText = await response.text()
          if (errorText) {
            try {
              const errorJson = JSON.parse(errorText)
              errorMessage = errorJson.message || errorJson.error || errorMessage
            } catch {
              errorMessage = errorText.length < 100 ? errorText : errorMessage
            }
          }
        } catch {
          // If we can't read error text, use default message
        }
        
        if (response.status === 404) {
          errorMessage = "Export cart is empty. Please add calculations to the cart first."
        }
        
        throw new Error(errorMessage)
      }

      // Check content type
      const contentType = response.headers.get('content-type')
      if (contentType && !contentType.includes('text/csv') && !contentType.includes('application/octet-stream')) {
        console.warn('Unexpected content type:', contentType)
      }

      const blob = await response.blob()
      
      // Check if blob is empty
      if (blob.size === 0) {
        throw new Error("Received empty CSV file. The export cart may be empty.")
      }

      // Create download link
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `tariff-calculations-${new Date().toISOString().split('T')[0]}.csv`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      setSuccessMessage("Successfully downloaded all cart items as CSV")
      setTimeout(() => setSuccessMessage(""), 3000)
    } catch (err) {
      console.error("Error downloading CSV:", err)
      setError(`Failed to download CSV: ${err instanceof Error ? err.message : 'Unknown error'}`)
      setTimeout(() => setError(""), 5000)
    }
  }

  if (isLoading) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center h-64">
          <p className="text-muted-foreground">Loading export cart...</p>
        </CardContent>
      </Card>
    )
  }

  return (
    <div>
      {error && (
        <Alert variant="destructive" className="mb-4">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {successMessage && (
        <Alert className="mb-4">
          <AlertDescription>{successMessage}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardHeader>
          <div className="flex flex-row items-center justify-between">
            <div>
              <CardTitle className="text-xl font-semibold text-foreground">Export Cart</CardTitle>
              <CardDescription>
                Review and export your tariff calculations ({cartItems.length} item{cartItems.length !== 1 ? 's' : ''})
              </CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {cartItems.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-muted-foreground">Your export cart is empty</p>
              <p className="text-sm text-muted-foreground mt-2">
                Add calculations from the calculator to export them
              </p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="border-b border-border">
                      <th className="text-left py-3 px-4 font-medium text-muted-foreground w-12">
                        <Checkbox
                          checked={selectedItems.size === cartItems.length && cartItems.length > 0}
                          onCheckedChange={handleSelectAll}
                        />
                      </th>
                      <th className="text-left py-3 px-4 font-medium text-muted-foreground">Product</th>
                      <th className="text-left py-3 px-4 font-medium text-muted-foreground">Brand</th>
                      <th className="text-left py-3 px-4 font-medium text-muted-foreground">Route</th>
                      <th className="text-left py-3 px-4 font-medium text-muted-foreground">Quantity</th>
                      <th className="text-left py-3 px-4 font-medium text-muted-foreground">Product Cost</th>
                      <th className="text-left py-3 px-4 font-medium text-muted-foreground">Total Cost</th>
                      <th className="text-left py-3 px-4 font-medium text-muted-foreground">Tariff Type</th>
                      <th className="text-left py-3 px-4 font-medium text-muted-foreground">Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cartItems.map((item) => (
                      <tr key={item.calculationId} className="border-b border-border hover:bg-muted/50">
                        <td className="py-3 px-4">
                          <Checkbox
                            checked={selectedItems.has(item.calculationId)}
                            onCheckedChange={() => handleSelectItem(item.calculationId)}
                          />
                        </td>
                        <td className="py-3 px-4 text-foreground font-medium">{item.product}</td>
                        <td className="py-3 px-4 text-muted-foreground">{item.brand}</td>
                        <td className="py-3 px-4 text-muted-foreground">
                          {item.exportingFrom} → {item.importingTo}
                        </td>
                        <td className="py-3 px-4 text-muted-foreground">
                          {item.quantity} {item.unit}
                        </td>
                        <td className="py-3 px-4 text-muted-foreground">${item.productCost.toFixed(2)}</td>
                        <td className="py-3 px-4 text-foreground font-medium">${item.totalCost.toFixed(2)}</td>
                        <td className="py-3 px-4">
                          <Badge variant="secondary">{item.tariffType}</Badge>
                        </td>
                        <td className="py-3 px-4 text-muted-foreground">{item.calculationDate}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="flex justify-between items-center mt-6 pt-4 border-t border-border">
                <div className="flex gap-2">
                  <Button
                    onClick={handleDeleteSelected}
                    disabled={selectedItems.size === 0}
                    variant="destructive"
                    size="sm"
                  >
                    <Trash2 className="h-4 w-4 mr-2" />
                    Delete Selected ({selectedItems.size})
                  </Button>
                  <Button
                    onClick={handleClearAll}
                    variant="outline"
                    size="sm"
                  >
                    Clear All
                  </Button>
                </div>
                <Button
                  onClick={handleDownloadClick}
                  className="bg-accent hover:bg-accent/90 text-accent-foreground"
                  size="sm"
                >
                  <Download className="h-4 w-4 mr-2" />
                  {selectedItems.size > 0 
                    ? `Download Selected (${selectedItems.size})` 
                    : 'Download as CSV'}
                </Button>
              </div>
            </>
          )}
        </CardContent>
      </Card>

      <AlertDialog open={showDownloadDialog} onOpenChange={setShowDownloadDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Download All Items?</AlertDialogTitle>
            <AlertDialogDescription>
              No items are selected. Do you want to download all {cartItems.length} item{cartItems.length !== 1 ? 's' : ''} in your cart as a CSV file?
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDownloadCSV}>Yes, Download All</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}