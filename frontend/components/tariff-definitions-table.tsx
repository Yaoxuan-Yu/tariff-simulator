"use client"

import { useState, useEffect } from "react"
import supabase from "@/lib/supabaseClient"
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
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { Download, Plus, Trash2 } from "lucide-react"

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

interface TariffDefinitionsTableProps {
  userRole: "admin" | "general"
  simulatorMode?: boolean
}

export function TariffDefinitionsTable({ userRole, simulatorMode = false }: TariffDefinitionsTableProps) {
  const [globalTariffs, setGlobalTariffs] = useState<TariffDefinition[]>([])
  const [modifiedGlobalTariffs, setModifiedGlobalTariffs] = useState<TariffDefinition[]>([])
  const [userTariffs, setUserTariffs] = useState<TariffDefinition[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [alertDialog, setAlertDialog] = useState({ open: false, title: "", message: "" })

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

  const [countries, setCountries] = useState<string[]>([])
  const [products, setProducts] = useState<string[]>([])

  useEffect(() => {
    const loadTariffs = async () => {
      try {
        const { data: { session } } = await supabase.auth.getSession()
        const token = session?.access_token
        const authHeaders = {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        }

        if (!simulatorMode) {
          // Load global tariffs
          const globalRes = await fetch("http://localhost:8080/api/tariff-definitions/global", { 
            headers: authHeaders,
            credentials: 'include'
          })
          if (!globalRes.ok) throw new Error(`Global defs failed ${globalRes.status}`)
          const globalData: TariffDefinitionsResponse = await globalRes.json()
          
          if (globalData.success && globalData.data) {
            setGlobalTariffs(globalData.data)
          }

          // Load modified global tariffs for admins
          if (userRole === "admin") {
            const modifiedRes = await fetch("http://localhost:8080/api/tariff-definitions/modified", { 
              headers: authHeaders,
              credentials: 'include'
            })
            if (modifiedRes.ok) {
              const modifiedData: TariffDefinitionsResponse = await modifiedRes.json()
              if (modifiedData.success && modifiedData.data) {
                setModifiedGlobalTariffs(modifiedData.data)
              }
            }
          }
        }

        if (simulatorMode) {
          // Load user-defined tariffs for simulator mode
          const userRes = await fetch("http://localhost:8080/api/tariff-definitions/user", { 
            headers: authHeaders,
            credentials: 'include'
          })
          if (!userRes.ok) throw new Error(`User defs failed ${userRes.status}`)
          const userData: TariffDefinitionsResponse = await userRes.json()
          
          if (userData.success && userData.data) {
            setUserTariffs(userData.data)
          }
        }

        // Load countries and products
        const [countriesRes, productsRes] = await Promise.all([
          fetch("http://localhost:8080/api/countries", { 
            headers: authHeaders,
            credentials: 'include'
          }),
          fetch("http://localhost:8080/api/products", { 
            headers: authHeaders,
            credentials: 'include'
          }),
        ])

        if (!countriesRes.ok) throw new Error(`Countries failed ${countriesRes.status}`)
        if (!productsRes.ok) throw new Error(`Products failed ${productsRes.status}`)

        const countriesList: string[] = await countriesRes.json()
        const productsList: string[] = await productsRes.json()

        setCountries(countriesList)
        setProducts(productsList)
      } catch (err) {
        showAlert("Error", "An unexpected error occurred while loading tariffs")
      } finally {
        setIsLoading(false)
      }
    }

    loadTariffs()
  }, [userRole, simulatorMode])

  const showAlert = (title: string, message: string) => {
    setAlertDialog({ open: true, title, message })
  }

  const handleAddTariff = async () => {
    // Validation
    if (
      !newTariff.product ||
      !newTariff.exportingFrom ||
      !newTariff.importingTo ||
      !newTariff.type ||
      !newTariff.rate ||
      !newTariff.effectiveDate ||
      !newTariff.expirationDate
    ) {
      showAlert("Incomplete Information", "Please fill in all required fields before submitting.")
      return
    }

    const rateValue = Number.parseFloat(newTariff.rate)
    if (isNaN(rateValue) || rateValue < 0) {
      showAlert("Invalid Rate", "Please enter a valid positive number for the tariff rate.")
      return
    }

    if (newTariff.exportingFrom === newTariff.importingTo) {
      showAlert("Invalid Country Pair", "Exporting and importing countries must be different.")
      return
    }

    const payload = {
      id: `${simulatorMode || userRole === "general" ? "user" : "admin-modified"}-${Date.now()}`,
      product: newTariff.product,
      exportingFrom: newTariff.exportingFrom,
      importingTo: newTariff.importingTo,
      type: newTariff.type,
      rate: rateValue,
      effectiveDate: newTariff.effectiveDate,
      expirationDate: newTariff.expirationDate,
    }

    try {
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token

      let endpoint = "http://localhost:8080/api/tariff-definitions/user"
      if (!simulatorMode && userRole === "admin") {
        endpoint = "http://localhost:8080/api/tariff-definitions/modified"
      }

      const res = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          'Authorization': token ? `Bearer ${token}` : ''
        },
        credentials: 'include',
        body: JSON.stringify(payload),
      })

      if (!res.ok) throw new Error(`Add tariff failed ${res.status}`)
      const response: TariffDefinitionsResponse = await res.json()

      if (!response.success || !response.data) {
        throw new Error(response.error || "Add failed")
      }

      if (simulatorMode) {
        // Reload tariffs from backend to ensure we have all of them
        const userRes = await fetch("http://localhost:8080/api/tariff-definitions/user", {
          headers: {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
          },
          credentials: 'include'
        })
        if (userRes.ok) {
          const userData: TariffDefinitionsResponse = await userRes.json()
          if (userData.success && userData.data) {
            setUserTariffs(userData.data)
          }
        }
        showAlert("Success", "Simulated tariff has been successfully added.")
      } else if (userRole === "admin") {
        setModifiedGlobalTariffs((prev) => [...prev, ...response.data!])
        
        // Check if replaced existing
        const replaced = modifiedGlobalTariffs.some(
          t => t.product === payload.product && 
               t.exportingFrom === payload.exportingFrom && 
               t.importingTo === payload.importingTo
        )
        
        if (replaced) {
          showAlert(
            "Tariff Updated",
            `The global tariff for ${payload.product} from ${payload.exportingFrom} to ${payload.importingTo} has been updated.`
          )
        } else {
          showAlert("Success", "New global tariff has been successfully added to the system.")
        }
      }

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
    } catch (e) {
      showAlert("Error", "Failed to add tariff. Please try again.")
    }
  }

  const handleDeleteTariff = async (id: string, isModifiedGlobal: boolean) => {
    try {
      const { data: { session } } = await supabase.auth.getSession()
      const token = session?.access_token

      const endpoint = isModifiedGlobal 
        ? `http://localhost:8080/api/tariff-definitions/modified/${id}`
        : `http://localhost:8080/api/tariff-definitions/user/${id}`

      const res = await fetch(endpoint, {
        method: "DELETE",
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      })

      if (!res.ok) throw new Error(`Delete failed ${res.status}`)

      if (isModifiedGlobal) {
        // Reload modified tariffs from backend
        const modifiedRes = await fetch("http://localhost:8080/api/tariff-definitions/modified", {
          headers: {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
          },
          credentials: 'include'
        })
        if (modifiedRes.ok) {
          const modifiedData: TariffDefinitionsResponse = await modifiedRes.json()
          if (modifiedData.success && modifiedData.data) {
            setModifiedGlobalTariffs(modifiedData.data)
          }
        }
        showAlert("Deleted", "Modified global tariff has been successfully deleted.")
      } else {
        // Reload user tariffs from backend
        const userRes = await fetch("http://localhost:8080/api/tariff-definitions/user", {
          headers: {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
          },
          credentials: 'include'
        })
        if (userRes.ok) {
          const userData: TariffDefinitionsResponse = await userRes.json()
          if (userData.success && userData.data) {
            setUserTariffs(userData.data)
          }
        }
        showAlert("Deleted", "User-defined tariff has been successfully deleted.")
      }
    } catch (e) {
      showAlert("Error", "Failed to delete tariff. Please try again.")
    }
  }

  const handleExportCSV = () => {
    // Backend will handle CSV export
    window.location.href = 'http://localhost:8080/api/tariff-definitions/export'
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

  const renderTariffTable = (
    tariffs: TariffDefinition[],
    title: string,
    description: string,
    showActions = false,
    isModifiedGlobal = false,
  ) => {
    const displayTariffs = getFilteredTariffs(tariffs)

    if (tariffs.length === 0 && showActions) {
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
                  {showActions && <th className="text-left py-3 px-4 font-medium text-muted-foreground">Actions</th>}
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
                    {showActions && (
                      <td className="py-3 px-4">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDeleteTariff(tariff.id, isModifiedGlobal)}
                          className="text-destructive hover:text-destructive hover:bg-destructive/10"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </td>
                    )}
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

  // Merge modified global tariffs into global tariffs for display
  const mergedGlobalTariffs = [...globalTariffs]
  modifiedGlobalTariffs.forEach((modifiedTariff) => {
    const existingIndex = mergedGlobalTariffs.findIndex(
      (t) =>
        t.product === modifiedTariff.product &&
        t.exportingFrom === modifiedTariff.exportingFrom &&
        t.importingTo === modifiedTariff.importingTo,
    )
    if (existingIndex !== -1) {
      mergedGlobalTariffs[existingIndex] = modifiedTariff
    } else {
      mergedGlobalTariffs.push(modifiedTariff)
    }
  })

  return (
    <div>
      <AlertDialog open={alertDialog.open} onOpenChange={(open) => setAlertDialog({ ...alertDialog, open })}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{alertDialog.title}</AlertDialogTitle>
            <AlertDialogDescription>{alertDialog.message}</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogAction onClick={() => setAlertDialog({ ...alertDialog, open: false })}>
              OK
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <div className="flex justify-between items-center mb-6">
        <div className="flex space-x-2">
          <Button onClick={handleExportCSV} variant="outline" size="sm">
            <Download className="h-4 w-4 mr-2" />
            Export CSV
          </Button>
          {(simulatorMode || userRole === "admin") && (
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button className="bg-accent hover:bg-accent/90 text-accent-foreground" size="sm">
                  <Plus className="h-4 w-4 mr-2" />
                  {simulatorMode ? "Define Simulated Tariff" : "Define/Edit Global Tariff"}
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[500px]">
                <DialogHeader>
                  <DialogTitle>
                    {simulatorMode ? "Define Simulated Tariff" : "Define/Edit Global Tariff"}
                  </DialogTitle>
                  <DialogDescription>
                    {simulatorMode
                      ? "Create a temporary tariff for simulation purposes. This will not affect global tariffs."
                      : "Add or update a global tariff in the system. If a tariff already exists for the same product and country pair, it will be replaced."}
                  </DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-4 max-h-[60vh] overflow-y-auto">
                  <div className="grid gap-2">
                    <Label htmlFor="product">Product *</Label>
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
                    <Label htmlFor="exportingFrom">Exporting From *</Label>
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
                    <Label htmlFor="importingTo">Importing To *</Label>
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
                    <Label htmlFor="type">Tariff Type *</Label>
                    <Select
                      value={newTariff.type}
                      onValueChange={(value) => setNewTariff((prev) => ({ ...prev, type: value }))}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select tariff type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="AHS">AHS (Harmonized System)</SelectItem>
                        <SelectItem value="MFN">MFN (Most Favored Nation)</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="rate">Tariff Rate (%) *</Label>
                    <Input
                      id="rate"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="e.g., 5.25"
                      value={newTariff.rate}
                      onChange={(e) => setNewTariff((prev) => ({ ...prev, rate: e.target.value }))}
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="effectiveDate">Effective Date *</Label>
                    <Input
                      id="effectiveDate"
                      type="date"
                      value={newTariff.effectiveDate}
                      onChange={(e) => setNewTariff((prev) => ({ ...prev, effectiveDate: e.target.value }))}
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="expirationDate">Expiration Date *</Label>
                    <Input
                      id="expirationDate"
                      placeholder='e.g., "Ongoing" or "2025-12-31"'
                      value={newTariff.expirationDate}
                      onChange={(e) => setNewTariff((prev) => ({ ...prev, expirationDate: e.target.value }))}
                    />
                    <p className="text-xs text-muted-foreground">Enter "Ongoing" for indefinite tariffs</p>
                  </div>
                </div>
                <DialogFooter>
                  <Button variant="outline" onClick={() => setIsDialogOpen(false)}>
                    Cancel
                  </Button>
                  <Button type="submit" onClick={handleAddTariff}>
                    {simulatorMode ? "Add Simulated Tariff" : "Save Global Tariff"}
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          )}
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

      {simulatorMode ? (
        <>
          {userTariffs.length > 0 ? (
            renderTariffTable(
              userTariffs,
              "Simulated Tariffs",
              "Temporary tariffs for testing different scenarios. These do not affect global data.",
              true,
              false,
            )
          ) : (
            <Card>
              <CardContent className="flex flex-col items-center justify-center h-48 text-center">
                <p className="text-muted-foreground mb-4">No simulated tariffs defined yet.</p>
                <p className="text-sm text-muted-foreground">
                  Click "Define Simulated Tariff" above to create temporary tariffs for testing.
                </p>
              </CardContent>
            </Card>
          )}
        </>
      ) : (
        <>
          {renderTariffTable(
            mergedGlobalTariffs,
            "Global Tariffs",
            userRole === "admin"
              ? "System tariffs that can be edited by administrators. Modified tariffs are highlighted."
              : "Standard tariffs from the global database.",
            userRole === "admin",
            false,
          )}
        </>
      )}
    </div>
  )
}
