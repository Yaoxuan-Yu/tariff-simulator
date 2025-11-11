"use client"

import type React from "react"

import { useState } from "react"
import { Search, ExternalLink, ChevronDown, ChevronUp, FileText, Scale, TrendingUp } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Skeleton } from "@/components/ui/skeleton"
import supabase from "@/lib/supabaseClient"

const API_BASE_URL = "http://localhost:8080/api"

interface NewsArticle {
  id: string
  title: string
  summary: string
  publishedDate: string
  source?: string
  url?: string
  content?: string
}

interface TradeAgreement {
  id: string
  title: string
  description: string
  url?: string
  countries?: string[]
  effectiveDate?: string
}

export function TradeInsights() {
  const [searchQuery, setSearchQuery] = useState("")
  const [newsArticles, setNewsArticles] = useState<NewsArticle[]>([])
  const [tradeAgreements, setTradeAgreements] = useState<TradeAgreement[]>([])
  const [isLoadingNews, setIsLoadingNews] = useState(false)
  const [isLoadingAgreements, setIsLoadingAgreements] = useState(false)
  const [hasSearched, setHasSearched] = useState(false)
  const [expandedArticle, setExpandedArticle] = useState<string | null>(null)
  const [expandedAgreement, setExpandedAgreement] = useState<string | null>(null)
  const [selectedArticle, setSelectedArticle] = useState<NewsArticle | null>(null)
  const [selectedAgreement, setSelectedAgreement] = useState<TradeAgreement | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)

  const getAuthToken = async (): Promise<string> => {
    const {
      data: { session },
    } = await supabase.auth.getSession()
    return session?.access_token || ""
  }

  const createAuthHeaders = (token: string): Record<string, string> => {
    return {
      Authorization: token ? `Bearer ${token}` : "",
      "Content-Type": "application/json",
    }
  }

  const handleSearch = async () => {
    if (!searchQuery.trim()) return

    setHasSearched(true)
    setIsLoadingNews(true)
    setIsLoadingAgreements(true)

    try {
      const token = await getAuthToken()
      const headers = createAuthHeaders(token)

      // Fetch news articles
      const newsResponse = await fetch(`${API_BASE_URL}/trade-insights/news?query=${encodeURIComponent(searchQuery)}`, {
        method: "GET",
        headers,
        credentials: "include",
      })

      if (newsResponse.ok) {
        const newsData = await newsResponse.json()
        setNewsArticles(Array.isArray(newsData) ? newsData : [])
      } else {
        setNewsArticles([])
      }
    } catch (error) {
      console.error("Error fetching news:", error)
      setNewsArticles([])
    } finally {
      setIsLoadingNews(false)
    }

    try {
      const token = await getAuthToken()
      const headers = createAuthHeaders(token)

      // Fetch trade agreements
      const agreementsResponse = await fetch(
        `${API_BASE_URL}/trade-insights/agreements?query=${encodeURIComponent(searchQuery)}`,
        {
          method: "GET",
          headers,
          credentials: "include",
        },
      )

      if (agreementsResponse.ok) {
        const agreementsData = await agreementsResponse.json()
        setTradeAgreements(Array.isArray(agreementsData) ? agreementsData : [])
      } else {
        setTradeAgreements([])
      }
    } catch (error) {
      console.error("Error fetching agreements:", error)
      setTradeAgreements([])
    } finally {
      setIsLoadingAgreements(false)
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleSearch()
    }
  }

  const toggleArticle = (id: string) => {
    setExpandedArticle(expandedArticle === id ? null : id)
  }

  const toggleAgreement = (id: string) => {
    setExpandedAgreement(expandedAgreement === id ? null : id)
  }

  const openArticleModal = (article: NewsArticle) => {
    setSelectedArticle(article)
    setSelectedAgreement(null)
    setIsModalOpen(true)
  }

  const openAgreementModal = (agreement: TradeAgreement) => {
    setSelectedAgreement(agreement)
    setSelectedArticle(null)
    setIsModalOpen(true)
  }

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleDateString("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
      })
    } catch {
      return dateString
    }
  }

  return (
    <div className="space-y-8">
      {/* Search Section */}
      <Card className="border-2 shadow-sm">
        <CardHeader>
          <CardTitle className="text-2xl flex items-center gap-2">
            <TrendingUp className="h-6 w-6 text-primary" />
            Search Trade Insights
          </CardTitle>
          <CardDescription>
            Search for trade-related news articles and official trade agreements by keyword (e.g., product, country)
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-3">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                type="text"
                placeholder='Try "Malaysia tariff", "steel", or "Singapore export"...'
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={handleKeyPress}
                className="pl-10 h-12 text-base"
              />
            </div>
            <Button onClick={handleSearch} disabled={!searchQuery.trim()} size="lg" className="px-8">
              Search
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* News Articles Section */}
      {hasSearched && (
        <div className="space-y-4">
          <div className="flex items-center gap-3 pb-2 border-b-2">
            <FileText className="h-5 w-5 text-primary" />
            <h2 className="text-2xl font-bold text-foreground">News Articles</h2>
            {!isLoadingNews && <Badge variant="secondary">{newsArticles.length} results</Badge>}
          </div>

          {isLoadingNews ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <Card key={i} className="border shadow-sm">
                  <CardHeader>
                    <Skeleton className="h-6 w-3/4 mb-2" />
                    <Skeleton className="h-4 w-1/4" />
                  </CardHeader>
                  <CardContent>
                    <Skeleton className="h-4 w-full mb-2" />
                    <Skeleton className="h-4 w-5/6" />
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : newsArticles.length === 0 ? (
            <Card className="border-dashed border-2">
              <CardContent className="flex flex-col items-center justify-center py-12">
                <FileText className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-lg font-medium text-muted-foreground">No articles found</p>
                <p className="text-sm text-muted-foreground mt-1">Try a different search term</p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4">
              {newsArticles.map((article) => (
                <Card
                  key={article.id}
                  className="border shadow-sm hover:shadow-md transition-all cursor-pointer group"
                  onClick={() => toggleArticle(article.id)}
                >
                  <CardHeader>
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1 space-y-2">
                        <CardTitle className="text-xl group-hover:text-primary transition-colors leading-relaxed">
                          {article.title}
                        </CardTitle>
                        <div className="flex items-center gap-3 text-sm text-muted-foreground">
                          <span className="font-medium">{formatDate(article.publishedDate)}</span>
                          {article.source && (
                            <>
                              <span>•</span>
                              <Badge variant="outline" className="font-normal">
                                {article.source}
                              </Badge>
                            </>
                          )}
                        </div>
                      </div>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          toggleArticle(article.id)
                        }}
                      >
                        {expandedArticle === article.id ? (
                          <ChevronUp className="h-4 w-4" />
                        ) : (
                          <ChevronDown className="h-4 w-4" />
                        )}
                      </Button>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <p className="text-muted-foreground leading-relaxed">{article.summary}</p>

                    {expandedArticle === article.id && (
                      <div className="pt-4 border-t space-y-4 animate-in fade-in slide-in-from-top-2">
                        {article.content && (
                          <div className="prose prose-sm max-w-none">
                            <p className="text-foreground leading-relaxed whitespace-pre-line">{article.content}</p>
                          </div>
                        )}
                        <div className="flex gap-2">
                          <Button variant="outline" size="sm" onClick={() => openArticleModal(article)}>
                            <FileText className="h-4 w-4 mr-2" />
                            View Full Article
                          </Button>
                          {article.url && (
                            <Button variant="outline" size="sm" asChild>
                              <a href={article.url} target="_blank" rel="noopener noreferrer">
                                <ExternalLink className="h-4 w-4 mr-2" />
                                Open Source
                              </a>
                            </Button>
                          )}
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Trade Agreements Section */}
      {hasSearched && (
        <div className="space-y-4">
          <div className="flex items-center gap-3 pb-2 border-b-2">
            <Scale className="h-5 w-5 text-primary" />
            <h2 className="text-2xl font-bold text-foreground">Trade Agreements</h2>
            {!isLoadingAgreements && <Badge variant="secondary">{tradeAgreements.length} results</Badge>}
          </div>

          {isLoadingAgreements ? (
            <div className="space-y-4">
              {[1, 2].map((i) => (
                <Card key={i} className="border shadow-sm bg-primary/5">
                  <CardHeader>
                    <Skeleton className="h-6 w-2/3 mb-2" />
                    <Skeleton className="h-4 w-1/3" />
                  </CardHeader>
                  <CardContent>
                    <Skeleton className="h-4 w-full mb-2" />
                    <Skeleton className="h-4 w-4/5" />
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : tradeAgreements.length === 0 ? (
            <Card className="border-dashed border-2">
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Scale className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-lg font-medium text-muted-foreground">No trade agreements found</p>
                <p className="text-sm text-muted-foreground mt-1">Try searching for a specific country or region</p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4">
              {tradeAgreements.map((agreement) => (
                <Card
                  key={agreement.id}
                  className="border-2 border-primary/20 shadow-sm hover:shadow-md transition-all cursor-pointer group bg-primary/5"
                  onClick={() => toggleAgreement(agreement.id)}
                >
                  <CardHeader>
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1 space-y-2">
                        <div className="flex items-center gap-2">
                          <Scale className="h-5 w-5 text-primary flex-shrink-0" />
                          <CardTitle className="text-xl group-hover:text-primary transition-colors leading-relaxed">
                            {agreement.title}
                          </CardTitle>
                        </div>
                        {agreement.effectiveDate && (
                          <div className="flex items-center gap-2 text-sm text-muted-foreground">
                            <span className="font-medium">Effective: {formatDate(agreement.effectiveDate)}</span>
                          </div>
                        )}
                        {agreement.countries && agreement.countries.length > 0 && (
                          <div className="flex flex-wrap gap-1.5">
                            {agreement.countries.map((country, idx) => (
                              <Badge key={idx} variant="secondary" className="text-xs">
                                {country}
                              </Badge>
                            ))}
                          </div>
                        )}
                      </div>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          toggleAgreement(agreement.id)
                        }}
                      >
                        {expandedAgreement === agreement.id ? (
                          <ChevronUp className="h-4 w-4" />
                        ) : (
                          <ChevronDown className="h-4 w-4" />
                        )}
                      </Button>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <p className="text-foreground leading-relaxed">{agreement.description}</p>

                    {expandedAgreement === agreement.id && (
                      <div className="pt-4 border-t space-y-4 animate-in fade-in slide-in-from-top-2">
                        <div className="flex gap-2">
                          <Button variant="default" size="sm" onClick={() => openAgreementModal(agreement)}>
                            <FileText className="h-4 w-4 mr-2" />
                            View Details
                          </Button>
                          {agreement.url && (
                            <Button variant="outline" size="sm" asChild>
                              <a href={agreement.url} target="_blank" rel="noopener noreferrer">
                                <ExternalLink className="h-4 w-4 mr-2" />
                                Open Full Agreement
                              </a>
                            </Button>
                          )}
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Modal for Full View */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="max-w-3xl max-h-[80vh] overflow-y-auto">
          {selectedArticle && (
            <>
              <DialogHeader>
                <DialogTitle className="text-2xl leading-relaxed pr-8">{selectedArticle.title}</DialogTitle>
                <DialogDescription className="flex items-center gap-3 pt-2">
                  <span className="font-medium">{formatDate(selectedArticle.publishedDate)}</span>
                  {selectedArticle.source && (
                    <>
                      <span>•</span>
                      <Badge variant="outline">{selectedArticle.source}</Badge>
                    </>
                  )}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 pt-4">
                <div className="prose prose-sm max-w-none">
                  <p className="text-muted-foreground text-base leading-relaxed">{selectedArticle.summary}</p>
                  {selectedArticle.content && (
                    <div className="mt-4 pt-4 border-t">
                      <p className="text-foreground leading-relaxed whitespace-pre-line">{selectedArticle.content}</p>
                    </div>
                  )}
                </div>
                {selectedArticle.url && (
                  <div className="pt-4">
                    <Button variant="default" asChild>
                      <a href={selectedArticle.url} target="_blank" rel="noopener noreferrer">
                        <ExternalLink className="h-4 w-4 mr-2" />
                        Read Full Article at Source
                      </a>
                    </Button>
                  </div>
                )}
              </div>
            </>
          )}

          {selectedAgreement && (
            <>
              <DialogHeader>
                <DialogTitle className="text-2xl leading-relaxed pr-8 flex items-center gap-2">
                  <Scale className="h-6 w-6 text-primary" />
                  {selectedAgreement.title}
                </DialogTitle>
                {selectedAgreement.effectiveDate && (
                  <DialogDescription className="pt-2">
                    <span className="font-medium">Effective: {formatDate(selectedAgreement.effectiveDate)}</span>
                  </DialogDescription>
                )}
              </DialogHeader>
              <div className="space-y-4 pt-4">
                {selectedAgreement.countries && selectedAgreement.countries.length > 0 && (
                  <div className="flex flex-wrap gap-2">
                    {selectedAgreement.countries.map((country, idx) => (
                      <Badge key={idx} variant="secondary">
                        {country}
                      </Badge>
                    ))}
                  </div>
                )}
                <div className="prose prose-sm max-w-none">
                  <p className="text-foreground text-base leading-relaxed">{selectedAgreement.description}</p>
                </div>
                {selectedAgreement.url && (
                  <div className="pt-4">
                    <Button variant="default" asChild>
                      <a href={selectedAgreement.url} target="_blank" rel="noopener noreferrer">
                        <ExternalLink className="h-4 w-4 mr-2" />
                        View Full Agreement Document
                      </a>
                    </Button>
                  </div>
                )}
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}
