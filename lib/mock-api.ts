

export interface Product {
  name: string
  brand: string
  cost: number
  unit: string
}

export interface TariffRate {
  country: string
  partner: string
  ahsWeighted: number
  mfnWeighted: number
  productCost?: number
}

export interface LoginResponse {
  success: boolean
  user?: {
    id: string
    name: string
    email: string
  }
  error?: string
}

export interface TariffCalculationResponse {
  success: boolean
  data?: {
    product: string
    brand: string
    exportingFrom: string
    importingTo: string
    quantity: number
    unit: string
    productCost: number
    totalCost: number
    tariffRate: number
    tariffType: string
    breakdown: Array<{
      description: string
      type: string
      rate: string
      amount: number
    }>
  }
  error?: string
}

export interface TariffDefinitionsResponse {
  success: boolean
  data?: Array<{
    id: string
    product: string
    exportingFrom: string
    importingTo: string
    type: string
    rate: number
    effectiveDate: string
    expirationDate: string
  }>
  error?: string
}

export const PRODUCTS: Product[] = [
  { name: "Rice (Paddy & Milled)", brand: "GoldenHarvest", cost: 12.3, unit: "kg" },
  { name: "Rice (Paddy & Milled)", brand: "PureGrain", cost: 9.7, unit: "kg" },
  { name: "Rice (Paddy & Milled)", brand: "SunFields", cost: 14.8, unit: "kg" },
  { name: "Wheat", brand: "FarmGold", cost: 8.5, unit: "kg" },
  { name: "Wheat", brand: "GrainWorks", cost: 10.2, unit: "kg" },
  { name: "Wheat", brand: "PrairieChoice", cost: 7.9, unit: "kg" },
  { name: "Sugar (Raw & Refined)", brand: "SweetVale", cost: 5.3, unit: "kg" },
  { name: "Sugar (Raw & Refined)", brand: "CaneBloom", cost: 6.9, unit: "kg" },
  { name: "Sugar (Raw & Refined)", brand: "SugarHaven", cost: 4.8, unit: "kg" },
  { name: "Palm Oil", brand: "OliveCrest", cost: 14.0, unit: "L" },
  { name: "Palm Oil", brand: "SunPress", cost: 12.1, unit: "L" },
  { name: "Palm Oil", brand: "NutriGold", cost: 10.9, unit: "L" },
  { name: "Coconut Oil", brand: "CocoPure", cost: 13.2, unit: "L" },
  { name: "Coconut Oil", brand: "TropiOil", cost: 11.8, unit: "L" },
  { name: "Coconut Oil", brand: "NutriCoco", cost: 10.5, unit: "L" },
  { name: "Coffee Beans", brand: "BeanCrafters", cost: 7.5, unit: "500g" },
  { name: "Coffee Beans", brand: "MorningRoast", cost: 8.1, unit: "500g" },
  { name: "Coffee Beans", brand: "JavaNest", cost: 9.2, unit: "500g" },
  { name: "Cocoa Beans", brand: "ChocoVale", cost: 9.6, unit: "500g" },
  { name: "Cocoa Beans", brand: "SweetNest", cost: 10.4, unit: "500g" },
  { name: "Cocoa Beans", brand: "CocoaBloom", cost: 8.9, unit: "500g" },
  { name: "Fresh Bananas", brand: "BananaJoy", cost: 6.1, unit: "kg" },
  { name: "Fresh Bananas", brand: "TropiBan", cost: 5.7, unit: "kg" },
  { name: "Fresh Bananas", brand: "SunnyBan", cost: 6.5, unit: "kg" },
  { name: "Fresh Pineapples", brand: "PineSweet", cost: 7.3, unit: "kg" },
  { name: "Fresh Pineapples", brand: "TropiPine", cost: 6.9, unit: "kg" },
  { name: "Fresh Pineapples", brand: "PineGold", cost: 7.5, unit: "kg" },
  { name: "Fresh or Dried Chillies", brand: "SpiceKing", cost: 4.5, unit: "500g" },
  { name: "Fresh or Dried Chillies", brand: "HotFlame", cost: 5.0, unit: "500g" },
  { name: "Fresh or Dried Chillies", brand: "RedPepper", cost: 4.8, unit: "500g" },
]

export const TARIFF_RATES: TariffRate[] = [
  { country: "Australia", partner: "China", ahsWeighted: 0.25, mfnWeighted: 3.33 },
  { country: "Australia", partner: "Indonesia", ahsWeighted: 0, mfnWeighted: 2.38 },
  { country: "Australia", partner: "India", ahsWeighted: 4.01, mfnWeighted: 4.01 },
  { country: "Australia", partner: "Japan", ahsWeighted: 0.1, mfnWeighted: 2.88 },
  { country: "Australia", partner: "Malaysia", ahsWeighted: 0.98, mfnWeighted: 2.8 },
  { country: "Australia", partner: "Philippines", ahsWeighted: 0.02, mfnWeighted: 3.8 },
  { country: "Australia", partner: "Singapore", ahsWeighted: 0.02, mfnWeighted: 3.41 },
  { country: "Australia", partner: "United States", ahsWeighted: 0, mfnWeighted: 3.29 },
  { country: "Australia", partner: "Vietnam", ahsWeighted: 0.02, mfnWeighted: 2.35 },
  { country: "China", partner: "Australia", ahsWeighted: 1.97, mfnWeighted: 10.29 },
  { country: "China", partner: "Indonesia", ahsWeighted: 1.52, mfnWeighted: 9.13 },
  { country: "China", partner: "India", ahsWeighted: 17.27, mfnWeighted: 18.75 },
  { country: "China", partner: "Japan", ahsWeighted: 12.05, mfnWeighted: 12.7 },
  { country: "China", partner: "Malaysia", ahsWeighted: 2.66, mfnWeighted: 10.74 },
  { country: "China", partner: "Philippines", ahsWeighted: 0.55, mfnWeighted: 6.98 },
  { country: "China", partner: "Singapore", ahsWeighted: 6.36, mfnWeighted: 12.56 },
  { country: "China", partner: "United States", ahsWeighted: 8.85, mfnWeighted: 8.85 },
  { country: "China", partner: "Vietnam", ahsWeighted: 0.66, mfnWeighted: 7.3 },
  { country: "India", partner: "Australia", ahsWeighted: 67.32, mfnWeighted: 67.32 },
  { country: "India", partner: "China", ahsWeighted: 29.3, mfnWeighted: 31.16 },
  { country: "India", partner: "Indonesia", ahsWeighted: 7.81, mfnWeighted: 33.59 },
  { country: "India", partner: "Japan", ahsWeighted: 97.33, mfnWeighted: 108.03 },
  { country: "India", partner: "Malaysia", ahsWeighted: 19.09, mfnWeighted: 42.91 },
  { country: "India", partner: "Philippines", ahsWeighted: 22.31, mfnWeighted: 26.3 },
  { country: "India", partner: "Singapore", ahsWeighted: 59.31, mfnWeighted: 70.51 },
  { country: "India", partner: "United States", ahsWeighted: 40.2, mfnWeighted: 40.2 },
  { country: "India", partner: "Vietnam", ahsWeighted: 8.46, mfnWeighted: 23.4 },
  { country: "Indonesia", partner: "Australia", ahsWeighted: 5.33, mfnWeighted: 10.69 },
  { country: "Indonesia", partner: "China", ahsWeighted: 5.56, mfnWeighted: 11.26 },
  { country: "Indonesia", partner: "India", ahsWeighted: 4.53, mfnWeighted: 8.12 },
  { country: "Indonesia", partner: "Japan", ahsWeighted: 17.61, mfnWeighted: 24.77 },
  { country: "Indonesia", partner: "Malaysia", ahsWeighted: 10.29, mfnWeighted: 19.9 },
  { country: "Indonesia", partner: "Philippines", ahsWeighted: 6.08, mfnWeighted: 14.14 },
  { country: "Indonesia", partner: "Singapore", ahsWeighted: 9.47, mfnWeighted: 21.33 },
  { country: "Indonesia", partner: "United States", ahsWeighted: 8.04, mfnWeighted: 8.04 },
  { country: "Indonesia", partner: "Vietnam", ahsWeighted: 1.38, mfnWeighted: 22.41 },
  { country: "Japan", partner: "Australia", ahsWeighted: 49.59, mfnWeighted: 73.29 },
  { country: "Japan", partner: "China", ahsWeighted: 10.67, mfnWeighted: 11.43 },
  { country: "Japan", partner: "Indonesia", ahsWeighted: 2.41, mfnWeighted: 3.85 },
  { country: "Japan", partner: "India", ahsWeighted: 3.97, mfnWeighted: 6.12 },
  { country: "Japan", partner: "Malaysia", ahsWeighted: 9.62, mfnWeighted: 12.24 },
  { country: "Japan", partner: "Philippines", ahsWeighted: 8.93, mfnWeighted: 12.46 },
  { country: "Japan", partner: "Singapore", ahsWeighted: 29.88, mfnWeighted: 32.59 },
  { country: "Japan", partner: "United States", ahsWeighted: 5.81, mfnWeighted: 2.66 },
  { country: "Japan", partner: "Vietnam", ahsWeighted: 6.77, mfnWeighted: 10.31 },
  { country: "Malaysia", partner: "Australia", ahsWeighted: 6.25, mfnWeighted: 6.25 },
  { country: "Malaysia", partner: "China", ahsWeighted: 5.28, mfnWeighted: 5.28 },
  { country: "Malaysia", partner: "Indonesia", ahsWeighted: 8.77, mfnWeighted: 8.77 },
  { country: "Malaysia", partner: "India", ahsWeighted: 4.66, mfnWeighted: 4.66 },
  { country: "Malaysia", partner: "Japan", ahsWeighted: 30.1, mfnWeighted: 30.1 },
  { country: "Malaysia", partner: "Philippines", ahsWeighted: 9.61, mfnWeighted: 9.61 },
  { country: "Malaysia", partner: "Singapore", ahsWeighted: 10.11, mfnWeighted: 10.11 },
  { country: "Malaysia", partner: "United States", ahsWeighted: 6.74, mfnWeighted: 6.74 },
  { country: "Malaysia", partner: "Vietnam", ahsWeighted: 3.5, mfnWeighted: 3.5 },
  { country: "Philippines", partner: "Australia", ahsWeighted: 0.06, mfnWeighted: 10.26 },
  { country: "Philippines", partner: "China", ahsWeighted: 3.12, mfnWeighted: 10.07 },
  { country: "Philippines", partner: "Indonesia", ahsWeighted: 0.02, mfnWeighted: 24.75 },
  { country: "Philippines", partner: "India", ahsWeighted: 3.2, mfnWeighted: 8.57 },
  { country: "Philippines", partner: "Japan", ahsWeighted: 0, mfnWeighted: 6.8 },
  { country: "Philippines", partner: "Malaysia", ahsWeighted: 0.1, mfnWeighted: 9.54 },
  { country: "Philippines", partner: "Singapore", ahsWeighted: 0, mfnWeighted: 7.12 },
  { country: "Philippines", partner: "United States", ahsWeighted: 4.1, mfnWeighted: 4.1 },
  { country: "Philippines", partner: "Vietnam", ahsWeighted: 0, mfnWeighted: 18.71 },
  { country: "United States", partner: "Australia", ahsWeighted: 3.43, mfnWeighted: 10.11 },
  { country: "United States", partner: "China", ahsWeighted: 7.5, mfnWeighted: 7.5 },
  { country: "United States", partner: "Indonesia", ahsWeighted: 3.54, mfnWeighted: 5.48 },
  { country: "United States", partner: "India", ahsWeighted: 6.18, mfnWeighted: 6.18 },
  { country: "United States", partner: "Japan", ahsWeighted: 4.63, mfnWeighted: 4.73 },
  { country: "United States", partner: "Malaysia", ahsWeighted: 6.78, mfnWeighted: 6.78 },
  { country: "United States", partner: "Philippines", ahsWeighted: 3.76, mfnWeighted: 5.33 },
  { country: "United States", partner: "Singapore", ahsWeighted: 0, mfnWeighted: 16.08 },
  { country: "United States", partner: "Vietnam", ahsWeighted: 4.59, mfnWeighted: 4.59 },
  { country: "Vietnam", partner: "Australia", ahsWeighted: 0.37, mfnWeighted: 39.74 },
  { country: "Vietnam", partner: "China", ahsWeighted: 6, mfnWeighted: 19.4 },
  { country: "Vietnam", partner: "Indonesia", ahsWeighted: 11.74, mfnWeighted: 46.32 },
  { country: "Vietnam", partner: "India", ahsWeighted: 0.8, mfnWeighted: 13.59 },
  { country: "Vietnam", partner: "Japan", ahsWeighted: 2.56, mfnWeighted: 17.52 },
  { country: "Vietnam", partner: "Malaysia", ahsWeighted: 1.16, mfnWeighted: 32.27 },
  { country: "Vietnam", partner: "Philippines", ahsWeighted: 5.64, mfnWeighted: 18.82 },
  { country: "Vietnam", partner: "Singapore", ahsWeighted: 0, mfnWeighted: 14.43 },
  { country: "Vietnam", partner: "United States", ahsWeighted: 8.43, mfnWeighted: 8.43 },
  { country: "Singapore", partner: "Australia", ahsWeighted: 0, mfnWeighted: 2.44 },
  { country: "Singapore", partner: "China", ahsWeighted: 0, mfnWeighted: 2.22 },
  { country: "Singapore", partner: "Indonesia", ahsWeighted: 0, mfnWeighted: 2.75 },
  { country: "Singapore", partner: "India", ahsWeighted: 0, mfnWeighted: 3.24 },
  { country: "Singapore", partner: "Japan", ahsWeighted: 0, mfnWeighted: 2.3 },
  { country: "Singapore", partner: "Malaysia", ahsWeighted: 0, mfnWeighted: 2.21 },
  { country: "Singapore", partner: "Philippines", ahsWeighted: 0, mfnWeighted: 4.02 },
  { country: "Singapore", partner: "United States", ahsWeighted: 0, mfnWeighted: 2.09 },
  { country: "Singapore", partner: "Vietnam", ahsWeighted: 0, mfnWeighted: 3.24 },
]

// Helper function to check if countries have FTA (Free Trade Agreement)
const hasFTA = (country: string, partner: string): boolean => {
  const tariff = TARIFF_RATES.find((t) => t.country === country && t.partner === partner)
  return tariff ? tariff.ahsWeighted < tariff.mfnWeighted : false
}

// Helper function to get unique countries
export const getCountries = (): string[] => {
  const countries = new Set<string>()
  TARIFF_RATES.forEach((rate) => {
    countries.add(rate.country)
    countries.add(rate.partner)
  })
  return Array.from(countries).sort()
}

// Helper function to get unique product names
export const getProductNames = (): string[] => {
  const names = new Set<string>()
  PRODUCTS.forEach((product) => names.add(product.name))
  return Array.from(names).sort()
}

// Helper function to get brands for a product
export const getBrandsForProduct = (productName: string): Product[] => {
  return PRODUCTS.filter((p) => p.name === productName)
}

// Mock login function
export const mockLogin = async (email: string, password: string): Promise<LoginResponse> => {
  // Simulate API delay
  await new Promise((resolve) => setTimeout(resolve, 1000))

  // Mock validation
  if (email === "jane.doe@example.com" && password === "password123") {
    return {
      success: true,
      user: {
        id: "1",
        name: "Jane Doe",
        email: "jane.doe@example.com",
      },
    }
  }

  return {
    success: false,
    error: "Invalid email or password",
  }
}

// Mock tariff calculation function
export const mockCalculateTariff = async (formData: any): Promise<TariffCalculationResponse> => {
  await new Promise((resolve) => setTimeout(resolve, 1500))

  const quantity = Number.parseFloat(formData.quantity) || 1
  const selectedProduct = PRODUCTS.find((p) => p.name === formData.product && p.brand === formData.brand)

  if (!selectedProduct) {
    return {
      success: false,
      error: "Product not found in database",
    }
  }

  const tariffData = TARIFF_RATES.find(
    (t) => t.country === formData.importingTo && t.partner === formData.exportingFrom,
  )

  if (!tariffData) {
    return {
      success: false,
      error: "Tariff data not available for this country pair",
    }
  }

  // Use custom cost if provided, otherwise use product cost
  const unitCost = formData.customCost ? Number.parseFloat(formData.customCost) : selectedProduct.cost
  const productCost = unitCost * quantity
  const hasFTAStatus = hasFTA(formData.importingTo, formData.exportingFrom)
  const tariffRate = hasFTAStatus ? tariffData.ahsWeighted : tariffData.mfnWeighted

  // Apply the correct formula: SUM(ProductCost * AHS/MFN) / SUM(ProductCost)
  // For single product calculation: (ProductCost * TariffRate) / ProductCost = TariffRate
  const effectiveTariffRate = tariffRate
  const tariffAmount = (productCost * effectiveTariffRate) / 100
  const totalCost = productCost + tariffAmount

  return {
    success: true,
    data: {
      product: selectedProduct.name,
      brand: selectedProduct.brand,
      exportingFrom: formData.exportingFrom,
      importingTo: formData.importingTo,
      quantity,
      unit: selectedProduct.unit,
      productCost,
      totalCost,
      tariffRate: effectiveTariffRate,
      tariffType: hasFTAStatus ? "AHS (with FTA)" : "MFN (no FTA)",
      breakdown: [
        {
          description: "Product Cost",
          type: "Base Cost",
          rate: "100%",
          amount: productCost,
        },
        {
          description: `Import Tariff (${hasFTAStatus ? "AHS" : "MFN"})`,
          type: "Tariff",
          rate: `${effectiveTariffRate.toFixed(2)}%`,
          amount: tariffAmount,
        },
      ],
    },
  }
}

// Mock tariff definitions function
export const mockGetTariffDefinitions = async (): Promise<TariffDefinitionsResponse> => {
  await new Promise((resolve) => setTimeout(resolve, 800))

  const sampleTariffs = [
    // Rice examples
    {
      id: "1",
      product: "Rice (Paddy & Milled)",
      exportingFrom: "China",
      importingTo: "Australia",
      type: "AHS",
      rate: 0.25, // AHS rate since 0.25 < 3.33 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    {
      id: "2",
      product: "Rice (Paddy & Milled)",
      exportingFrom: "India",
      importingTo: "Australia",
      type: "MFN",
      rate: 4.01, // MFN rate since AHS = MFN (no FTA benefit)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    // Wheat examples
    {
      id: "3",
      product: "Wheat",
      exportingFrom: "Indonesia",
      importingTo: "Australia",
      type: "AHS",
      rate: 0, // AHS rate since 0 < 2.38 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    {
      id: "4",
      product: "Wheat",
      exportingFrom: "United States",
      importingTo: "China",
      type: "MFN",
      rate: 8.85, // MFN rate since AHS = MFN (no FTA benefit)
      effectiveDate: "1/1/2023",
      expirationDate: "12/31/2024",
    },
    // Sugar examples
    {
      id: "5",
      product: "Sugar (Raw & Refined)",
      exportingFrom: "Malaysia",
      importingTo: "China",
      type: "AHS",
      rate: 2.66, // AHS rate since 2.66 < 10.74 (has FTA)
      effectiveDate: "1/1/2023",
      expirationDate: "Ongoing",
    },
    {
      id: "6",
      product: "Sugar (Raw & Refined)",
      exportingFrom: "United States",
      importingTo: "Malaysia",
      type: "MFN",
      rate: 6.74, // MFN rate since AHS = MFN (no FTA benefit)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    // Palm Oil examples
    {
      id: "7",
      product: "Palm Oil",
      exportingFrom: "Malaysia",
      importingTo: "Singapore",
      type: "AHS",
      rate: 0, // AHS rate since 0 < 2.21 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    {
      id: "8",
      product: "Palm Oil",
      exportingFrom: "Japan",
      importingTo: "Indonesia",
      type: "AHS",
      rate: 17.61, // AHS rate since 17.61 < 24.77 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    // Coconut Oil examples
    {
      id: "9",
      product: "Coconut Oil",
      exportingFrom: "Japan",
      importingTo: "Philippines",
      type: "AHS",
      rate: 0, // AHS rate since 0 < 6.8 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    {
      id: "10",
      product: "Coconut Oil",
      exportingFrom: "United States",
      importingTo: "Philippines",
      type: "AHS",
      rate: 4.1, // AHS rate since 4.1 = 4.1 (no FTA benefit, using MFN)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    // Coffee Beans examples
    {
      id: "11",
      product: "Coffee Beans",
      exportingFrom: "United States",
      importingTo: "Vietnam",
      type: "MFN",
      rate: 8.43, // MFN rate since AHS = MFN (no FTA benefit)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    {
      id: "12",
      product: "Coffee Beans",
      exportingFrom: "Japan",
      importingTo: "Vietnam",
      type: "AHS",
      rate: 2.56, // AHS rate since 2.56 < 17.52 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    // Cocoa Beans examples
    {
      id: "13",
      product: "Cocoa Beans",
      exportingFrom: "Australia",
      importingTo: "Indonesia",
      type: "AHS",
      rate: 5.33, // AHS rate since 5.33 < 10.69 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    {
      id: "14",
      product: "Cocoa Beans",
      exportingFrom: "United States",
      importingTo: "Indonesia",
      type: "MFN",
      rate: 8.04, // MFN rate since AHS = MFN (no FTA benefit)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    // Fresh Bananas examples
    {
      id: "15",
      product: "Fresh Bananas",
      exportingFrom: "Japan",
      importingTo: "Philippines",
      type: "AHS",
      rate: 0, // AHS rate since 0 < 6.8 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    {
      id: "16",
      product: "Fresh Bananas",
      exportingFrom: "United States",
      importingTo: "Philippines",
      type: "AHS",
      rate: 4.1, // AHS rate since 4.1 = 4.1 (no FTA benefit, using MFN)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    // Fresh Pineapples examples
    {
      id: "17",
      product: "Fresh Pineapples",
      exportingFrom: "Singapore",
      importingTo: "Philippines",
      type: "AHS",
      rate: 0, // AHS rate since 0 < 7.12 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    {
      id: "18",
      product: "Fresh Pineapples",
      exportingFrom: "China",
      importingTo: "Philippines",
      type: "AHS",
      rate: 0.55, // AHS rate since 0.55 < 6.98 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    // Fresh or Dried Chillies examples
    {
      id: "19",
      product: "Fresh or Dried Chillies",
      exportingFrom: "United States",
      importingTo: "India",
      type: "MFN",
      rate: 40.2, // MFN rate since AHS = MFN (no FTA benefit)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
    {
      id: "20",
      product: "Fresh or Dried Chillies",
      exportingFrom: "Indonesia",
      importingTo: "India",
      type: "AHS",
      rate: 7.81, // AHS rate since 7.81 < 33.59 (has FTA)
      effectiveDate: "1/1/2022",
      expirationDate: "Ongoing",
    },
  ]

  return {
    success: true,
    data: sampleTariffs,
  }
}
