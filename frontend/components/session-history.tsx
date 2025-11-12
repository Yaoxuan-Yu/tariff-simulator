"use client"

import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Checkbox } from "@/components/ui/checkbox"
import { Trash2, ShoppingCart, RefreshCw, Download } from "lucide-react"
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

interface HistoryItem {
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

export function SessionHistoryPage() {
  const [historyItems, setHistoryItems] = useState<HistoryItem[]>([])
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set())
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState("")
  const [successMessage, setSuccessMessage] = useState("")
  const [showDownloadDialog, setShowDownloadDialog] = useState(false)

  useEffect(() => {
    loadHistory()
  }, [])

  const loadHistory = async () => {
    try {
      setIsLoading(true)
      setError("")

      const response = await fetch("http://localhost:8080/api/tariff/history", {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      })

      if (response.status === 204) {
        setHistoryItems([])
        return
      }

      if (!response.ok) {
        throw new Error(`Failed to load history: ${response.statusText}`)
      }

      const result = await response.json()
      setHistoryItems(result || [])
    } catch (err) {
      console.error("Error loading history:", err)
      setError(err instanceof Error ? err.message : "An unexpected error occurred while loading session history")
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
    if (selectedItems.size === historyItems.length) {
      setSelectedItems(new Set())
    } else {
      setSelectedItems(new Set(historyItems.map((item) => item.calculationId)))
    }
  }

  const handleClearAll = async () => {
    if (!confirm("Are you sure you want to clear all history? This action cannot be undone.")) {
      return
    }

    try {
      setError("")

      const response = await fetch("http://localhost:8080/api/tariff/history/clear", {
        method: "DELETE",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      })

      if (!response.ok) {
        throw new Error("Failed to clear history")
      }

      setHistoryItems([])
      setSelectedItems(new Set())
      setSuccessMessage("All history cleared successfully")
      setTimeout(() => setSuccessMessage(""), 3000)
    } catch (err) {
      console.error("Error clearing history:", err)
      setError(err instanceof Error ? err.message : "An unexpected error occurred while clearing history")
    }
  }

  const handleAddToCart = async () => {
    if (selectedItems.size === 0) {
      setError("Please select at least one calculation to add to cart")
      setTimeout(() => setError(""), 3000)
      return
    }

    try {
      setError("")
      let successCount = 0
      let failCount = 0

      for (const calculationId of selectedItems) {
        try {
          const response = await fetch(`http://localhost:8080/api/export-cart/add/${calculationId}`, {
            method: "POST",
            credentials: "include",
            headers: {
              "Content-Type": "application/json",
            },
          })

          if (response.ok) {
            successCount++
          } else {
            failCount++
            console.error(`Failed to add ${calculationId} to cart`)
          }
        } catch (err) {
          failCount++
          console.error(`Error adding ${calculationId} to cart:`, err)
        }
      }

      if (successCount > 0) {
        setSuccessMessage(`Successfully added ${successCount} item${successCount !== 1 ? "s" : ""} to export cart`)
        setSelectedItems(new Set())
        setTimeout(() => setSuccessMessage(""), 3000)
      }

      if (failCount > 0) {
        setError(`Failed to add ${failCount} item${failCount !== 1 ? "s" : ""} to cart`)
        setTimeout(() => setError(""), 5000)
      }
    } catch (err) {
      console.error("Error adding to cart:", err)
      setError("An unexpected error occurred while adding to cart")
    }
  }

  const handleDownloadClick = () => {
    if (selectedItems.size === 0) {
      setShowDownloadDialog(true)
    } else {
      handleDownloadCSV()
    }
  }

  const handleDownloadCSV = () => {
    try {
      setError("")
      setShowDownloadDialog(false)

      const itemsToDownload =
        selectedItems.size > 0 ? historyItems.filter((item) => selectedItems.has(item.calculationId)) : historyItems

      if (itemsToDownload.length === 0) {
        setError("No items to download")
        return
      }

      // Create CSV content
      const headers = [
        "Product",
        "Brand",
        "Exporting From",
        "Importing To",
        "Quantity",
        "Unit",
        "Product Cost",
        "Total Cost",
        "Tariff Type",
        "Calculation Date",
      ]

      const rows = itemsToDownload.map((item) =>
        [
          item.product,
          item.brand,
          item.exportingFrom,
          item.importingTo,
          item.quantity,
          item.unit,
          item.productCost.toFixed(2),
          item.totalCost.toFixed(2),
          item.tariffType,
          item.calculationDate,
        ]
          .map((field) => `"${field}"`)
          .join(","),
      )

      const csv = [headers.join(","), ...rows].join("\n")

      // Download CSV
      const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.setAttribute("download", `session-history-${new Date().toISOString().split("T")[0]}.csv`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)

      setSuccessMessage(
        selectedItems.size > 0
          ? `Downloaded ${selectedItems.size} selected item${selectedItems.size !== 1 ? "s" : ""} as CSV`
          : "Downloaded all items as CSV",
      )
      setTimeout(() => setSuccessMessage(""), 3000)
    } catch (err) {
      console.error("Error downloading CSV:", err)
      setError("Failed to download CSV")
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-muted-foreground">Loading session history...</p>
      </div>
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

      <div className="flex items-center justify-between mb-4">
        <p className="text-sm text-muted-foreground">
          {historyItems.length} item{historyItems.length !== 1 ? "s" : ""} in history
        </p>
        <Button onClick={loadHistory} variant="outline" size="sm">
          <RefreshCw className="h-4 w-4 mr-2" />
          Refresh
        </Button>
      </div>

      {historyItems.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-muted-foreground">No calculation history</p>
          <p className="text-sm text-muted-foreground mt-2">Your calculations will appear here</p>
        </div>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-3 px-4 font-medium text-muted-foreground w-12">
                    <Checkbox
                      checked={selectedItems.size === historyItems.length && historyItems.length > 0}
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
                {historyItems.map((item) => (
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
                onClick={handleAddToCart}
                disabled={selectedItems.size === 0}
                className="bg-accent hover:bg-accent/90 text-accent-foreground"
                size="sm"
              >
                <ShoppingCart className="h-4 w-4 mr-2" />
                Add to Cart ({selectedItems.size})
              </Button>
              <Button onClick={handleClearAll} variant="destructive" size="sm">
                <Trash2 className="h-4 w-4 mr-2" />
                Clear All
              </Button>
            </div>
            <Button onClick={handleDownloadClick} variant="outline" size="sm">
              <Download className="h-4 w-4 mr-2" />
              {selectedItems.size > 0 ? `Download Selected (${selectedItems.size})` : "Download as CSV"}
            </Button>
          </div>
        </>
      )}

      <AlertDialog open={showDownloadDialog} onOpenChange={setShowDownloadDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Download All Items?</AlertDialogTitle>
            <AlertDialogDescription>
              No items are selected. Do you want to download all {historyItems.length} item
              {historyItems.length !== 1 ? "s" : ""} in your history as a CSV file?
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
