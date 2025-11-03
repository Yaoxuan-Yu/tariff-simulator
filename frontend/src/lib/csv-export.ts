// CSV export utility functions

export const exportToCSV = async (data: any, filename: string) => {
  try {
    // Create CSV content
    const csvContent = generateCSVContent(data)

    // Create blob and download
    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" })
    const link = document.createElement("a")

    if (link.download !== undefined) {
      const url = URL.createObjectURL(blob)
      link.setAttribute("href", url)
      link.setAttribute("download", `${filename}.csv`)
      link.style.visibility = "hidden"
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    } else {
      throw new Error("CSV download not supported in this browser")
    }
  } catch (error) {
    console.error("CSV export error:", error)
    throw new Error("Failed to export CSV file")
  }
}

const generateCSVContent = (data: any): string => {
  const headers = ["Description", "Type", "Rate", "Amount"]
  const rows = data.breakdown.map((item: any) => [item.description, item.type, item.rate, item.amount.toString()])

  // Add summary row
  rows.unshift(["Summary", "", "", ""])
  rows.unshift([
    `Product: ${data.product}`,
    `Route: ${data.exportingFrom} → ${data.importingTo}`,
    `Value: $${data.valueOfGoods.toLocaleString()}`,
    `Total: $${data.totalCost.toLocaleString()}`,
  ])
  rows.unshift(["", "", "", ""])

  // Convert to CSV format
  const csvRows = [headers, ...rows]
  return csvRows.map((row) => row.map((field) => `"${field.toString().replace(/"/g, '""')}"`).join(",")).join("\n")
}
