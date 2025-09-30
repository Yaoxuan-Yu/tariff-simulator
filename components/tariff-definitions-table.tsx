"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import {
  mockGetTariffDefinitions,
  getCountries,
  getProductNames,
  getUserDefinedTariffs,
  addUserDefinedTariff,
} from "@/lib/mock-api"
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

export function TariffDefinitionsTable() {
  const [globalTariffs, setGlobalTariffs] = useState<TariffDefinition[]>([])
  const [userTariffs, setUserTariffs] = useState<TariffDefinition[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState("")
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  const [filters, setFilters] = useState({
    product: "",
    exportingFrom: "",
    importingTo: "",
  })

  const [newTariff, setNewTariff] = useState({
    product: "",
    exportingFrom: "",
    importingTo: "",
    type: "",
    rate: "",
    effectiveDate: "",
    expirationDate: "",
  })

  const countries = getCountries()
  const products = getProductNames()

  useEffect(() => {
    const loadTariffs = async () => {
      try {
        const response = await mockGetTariffDefinitions()
        if (response.success && response.data) {
          setGlobalTariffs(response.data)
        } else {
          setError(response.error || "Failed to load tariff definitions")
        }

        setUserTariffs(getUserDefinedTariffs())
      } catch (err) {
        setError("An unexpected error occurred")
      } finally {
        setIsLoading(false)
      }
    }

    loadTariffs()
  }, [])

  const handleAddTariff = () => {
    if (
      !newTariff.product ||
      !newTariff.exportingFrom ||
      !newTariff.importingTo ||
      !newTariff.type ||
      !newTariff.rate ||
      !newTariff.effectiveDate ||
      !newTariff.expirationDate
    ) {
      setError("Please fill in all fields")
      return
    }

    const tariff: TariffDefinition = {
      id: `user-${Date.now()}`,
      product: newTariff.product,
      exportingFrom: newTariff.exportingFrom,
      importingTo: newTariff.importingTo,
      type: newTariff.type,
      rate: Number.parseFloat(newTariff.rate),
      effectiveDate: newTariff.effectiveDate,
      expirationDate: newTariff.expirationDate,
    }

    setUserTariffs((prev) => [...prev, tariff])
    addUserDefinedTariff(tariff)

    setNewTariff({
      product: "",
      exportingFrom: "",
      importingTo: "",
      type: "",
      rate: "",
      effectiveDate: "",
      expirationDate: "",
    })
    setIsDialogOpen(false)
    setError("")
  }

  const handleExportCSV = () => {
    // Do nothing - backend will handle CSV export
  }

  const getFilteredTariffs = (tariffs: TariffDefinition[]) => {
    return tariffs.filter((tariff) => {
      const matchesProduct = !filters.product || tariff.product === filters.product
      const matchesExporting = !filters.exportingFrom || tariff.exportingFrom === filters.exportingFrom
      const matchesImporting = !filters.importingTo || tariff.importingTo === filters.importingTo

      return matchesProduct && matchesExporting && matchesImporting
    })
  }

  const handleFilterChange = (field: string, value: string) => {
    setFilters((prev) => ({
      ...prev,
      [field]: value === "all" ? "" : value,
    }))
  }

  const clearFilters = () => {
    setFilters({
      product: "",
      exportingFrom: "",
      importingTo: "",
    })
  }

  const renderTariffTable = (tariffs: TariffDefinition[], title: string, description: string) => {
    const displayTariffs = getFilteredTariffs(tariffs)

    if (tariffs.length === 0 && title.includes("User")) {
      return null
    }

    return (
      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-xl font-semibold text-foreground">{title}</CardTitle>
          <CardDescription>{description}</CardDescription>
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
                {displayTariffs.map((tariff) => (
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
    <div>
      <div className="flex justify-between items-center mb-6">
        <div className="flex space-x-2">
          <Button onClick={handleExportCSV} variant="outline" size="sm">
            <Download className="h-4 w-4 mr-2" />
            Export CSV
          </Button>
          <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
            <DialogTrigger asChild>
              <Button className="bg-accent hover:bg-accent/90 text-accent-foreground" size="sm">
                <Plus className="h-4 w-4 mr-2" />
                Define New Tariff
              </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px]">
              <DialogHeader>
                <DialogTitle>Define New Tariff</DialogTitle>
                <DialogDescription>Add a new user-defined tariff to the system.</DialogDescription>
              </DialogHeader>
              <div className="grid gap-4 py-4">
                <div className="grid gap-2">
                  <Label htmlFor="product">Product</Label>
                  <Select
                    value={newTariff.product}
                    onValueChange={(value) => setNewTariff((prev) => ({ ...prev, product: value }))}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select product" />
                    </SelectTrigger>
                    <SelectContent>
                      {products.map((product) => (
                        <SelectItem key={product} value={product}>
                          {product}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="exportingFrom">Exporting From</Label>
                  <Select
                    value={newTariff.exportingFrom}
                    onValueChange={(value) => setNewTariff((prev) => ({ ...prev, exportingFrom: value }))}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select exporting country" />
                    </SelectTrigger>
                    <SelectContent>
                      {countries.map((country) => (
                        <SelectItem key={country} value={country}>
                          {country}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="importingTo">Importing To</Label>
                  <Select
                    value={newTariff.importingTo}
                    onValueChange={(value) => setNewTariff((prev) => ({ ...prev, importingTo: value }))}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select importing country" />
                    </SelectTrigger>
                    <SelectContent>
                      {countries.map((country) => (
                        <SelectItem key={country} value={country}>
                          {country}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="type">Type</Label>
                  <Select
                    value={newTariff.type}
                    onValueChange={(value) => setNewTariff((prev) => ({ ...prev, type: value }))}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select tariff type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="AHS">AHS</SelectItem>
                      <SelectItem value="MFN">MFN</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="rate">Rate (%)</Label>
                  <Input
                    id="rate"
                    type="number"
                    step="0.01"
                    placeholder="Enter rate percentage"
                    value={newTariff.rate}
                    onChange={(e) => setNewTariff((prev) => ({ ...prev, rate: e.target.value }))}
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="effectiveDate">Effective Date</Label>
                  <Input
                    id="effectiveDate"
                    type="date"
                    value={newTariff.effectiveDate}
                    onChange={(e) => setNewTariff((prev) => ({ ...prev, effectiveDate: e.target.value }))}
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="expirationDate">Expiration Date</Label>
                  <Input
                    id="expirationDate"
                    placeholder="e.g., Ongoing or specific date"
                    value={newTariff.expirationDate}
                    onChange={(e) => setNewTariff((prev) => ({ ...prev, expirationDate: e.target.value }))}
                  />
                </div>
              </div>
              <DialogFooter>
                <Button type="submit" onClick={handleAddTariff}>
                  Add Tariff
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-foreground">Filter Tariffs</CardTitle>
          <CardDescription>Filter tariffs by product, exporting country, or importing country.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label>Product</Label>
              <Select value={filters.product || "all"} onValueChange={(value) => handleFilterChange("product", value)}>
                <SelectTrigger>
                  <SelectValue placeholder="All products" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Products</SelectItem>
                  {products.map((product) => (
                    <SelectItem key={product} value={product}>
                      {product}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Exporting From</Label>
              <Select
                value={filters.exportingFrom || "all"}
                onValueChange={(value) => handleFilterChange("exportingFrom", value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="All countries" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Countries</SelectItem>
                  {countries.map((country) => (
                    <SelectItem key={country} value={country}>
                      {country}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Importing To</Label>
              <Select
                value={filters.importingTo || "all"}
                onValueChange={(value) => handleFilterChange("importingTo", value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="All countries" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Countries</SelectItem>
                  {countries.map((country) => (
                    <SelectItem key={country} value={country}>
                      {country}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <div className="mt-4">
            <Button onClick={clearFilters} variant="outline" size="sm">
              Clear All Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      {userTariffs.length > 0 &&
        renderTariffTable(userTariffs, "User Defined Tariffs", "Custom tariffs defined by users.")}
      {renderTariffTable(globalTariffs, "Global Tariffs", "Standard tariffs from the global database.")}
    </div>
  )
}
