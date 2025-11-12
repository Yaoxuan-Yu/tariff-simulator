"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";

type ActiveView = "news" | "agreements" | "combined";

interface TradeInsightsViewProps {
  apiBaseUrl: string;
  getAuthToken: () => Promise<string>;
}

interface NewsArticle {
  id?: string;
  title?: string;
  summary?: string;
  content?: string;
  source?: string;
  author?: string;
  articleUrl?: string;
  imageUrl?: string;
  publishedDate?: string;
}

interface NewsSearchResult {
  status?: string;
  articles?: NewsArticle[];
  totalResults?: number;
  pageSize?: number;
  page?: number;
}

interface AgreementDto {
  id?: string;
  title?: string;
  summary?: string;
  countries?: string[];
  agreementType?: string;
  documentUrl?: string;
  source?: string;
  publishedDate?: string;
  effectiveDate?: string;
}

interface AgreementSearchResult {
  status?: string;
  agreements?: AgreementDto[];
  totalResults?: number;
  pageSize?: number;
  page?: number;
}

interface TradeInsightsResult {
  status?: string;
  query?: string;
  country?: string;
  newsSection?: {
    title?: string;
    articles?: NewsArticle[];
    totalCount?: number;
  };
  agreementsSection?: {
    title?: string;
    agreements?: AgreementDto[];
    totalCount?: number;
  };
}

const formatDate = (value?: string) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
};

const buildPayload = (base: Record<string, unknown>) => {
  const payload: Record<string, unknown> = {};
  Object.entries(base).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      payload[key] = value;
    }
  });
  return payload;
};

export function TradeInsightsView({ apiBaseUrl, getAuthToken }: TradeInsightsViewProps) {
  const [request, setRequest] = useState({
    query: "",
    country: "",
    product: "",
    agreementType: "",
    limit: 10,
  });
  const [activeView, setActiveView] = useState<ActiveView>("news");
  const [loadingView, setLoadingView] = useState<ActiveView | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [newsResult, setNewsResult] = useState<NewsSearchResult | null>(null);
  const [agreementResult, setAgreementResult] = useState<AgreementSearchResult | null>(null);
  const [combinedResult, setCombinedResult] = useState<TradeInsightsResult | null>(null);

  const updateRequestField = (field: keyof typeof request, value: string | number) => {
    setRequest((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const fetchView = async (view: ActiveView) => {
    if ((view === "news" || view === "combined") && request.query.trim().length < 2) {
      setError("Please provide a query with at least 2 characters.");
      return;
    }
    if (view === "agreements" && request.country.trim().length === 0) {
      setError("Country is required when searching agreements.");
      return;
    }

    setActiveView(view);
    setLoadingView(view);
    setError(null);

    try {
      const token = await getAuthToken();
      const headers: Record<string, string> = {
        "Content-Type": "application/json",
      };
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      let endpoint = "/news/search";
      let payload: Record<string, unknown> = {};

      if (view === "news") {
        payload = buildPayload({
          query: request.query.trim(),
          country: request.country.trim(),
          product: request.product.trim(),
          limit: request.limit,
          offset: 0,
        });
        endpoint = "/news/search";
      } else if (view === "agreements") {
        payload = buildPayload({
          country: request.country.trim(),
          agreementType: request.agreementType.trim(),
          limit: request.limit,
          offset: 0,
        });
        endpoint = "/agreements/search";
      } else {
        payload = buildPayload({
          query: request.query.trim(),
          country: request.country.trim(),
          product: request.product.trim(),
          limit: request.limit,
        });
        endpoint = "/trade-insights/search";
      }

      const response = await fetch(`${apiBaseUrl}${endpoint}`, {
        method: "POST",
        headers,
        credentials: "include",
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        let errorMessage = "Request failed";
        try {
          const text = await response.text();
          if (text) {
            // Try to parse as JSON to extract a user-friendly message
            try {
              const errorJson = JSON.parse(text);
              // Extract a user-friendly message from the error response
              if (errorJson.message) {
                errorMessage = errorJson.message;
              } else if (errorJson.details?.error) {
                // Extract a cleaner message from nested error details
                const detailsError = errorJson.details.error;
                if (detailsError.includes("relation") && detailsError.includes("does not exist")) {
                  errorMessage = "Database table not available. Please contact support.";
                } else if (detailsError.includes("Database operation failed")) {
                  errorMessage = "Unable to access database. Please try again later.";
                } else {
                  errorMessage = detailsError;
                }
              } else if (errorJson.error) {
                errorMessage = errorJson.error;
              }
            } catch {
              // If JSON parsing fails, use the text as-is but clean it up
              errorMessage = text.length > 200 ? text.substring(0, 200) + "..." : text;
            }
          }
        } catch {
          errorMessage = `Server error (${response.status})`;
        }
        throw new Error(errorMessage);
      }

      const data = await response.json();

      if (view === "news") {
        setNewsResult(data);
      } else if (view === "agreements") {
        setAgreementResult(data);
      } else {
        setCombinedResult(data);
      }
    } catch (err) {
      console.error("Failed to fetch trade insights", err);
      setError(err instanceof Error ? err.message : "Unexpected error");
    } finally {
      setLoadingView(null);
    }
  };

  const renderNewsResults = () => {
    if (!newsResult || !newsResult.articles) {
      return <p className="text-sm text-slate-600">Search for news to see results.</p>;
    }

    if (!newsResult.articles.length) {
      return <p className="text-sm text-slate-600">No news articles found for this query.</p>;
    }

    return (
      <div className="space-y-4">
        {newsResult.articles.map((article, index) => (
          <Card key={article.id ?? index}>
            <CardHeader>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <CardTitle className="text-lg font-semibold">{article.title || "Untitled Article"}</CardTitle>
                  {article.source && <Badge className="mt-2">{article.source}</Badge>}
                </div>
                {article.publishedDate && (
                  <span className="text-xs text-slate-500 whitespace-nowrap">
                    {formatDate(article.publishedDate)}
                  </span>
                )}
              </div>
              {article.summary && (
                <CardDescription className="mt-2 text-sm text-slate-600">
                  {article.summary}
                </CardDescription>
              )}
            </CardHeader>
            <CardContent className="space-y-3">
              {article.content && (
                <p className="text-sm text-slate-700">
                  {article.content.length > 280 ? `${article.content.slice(0, 280)}...` : article.content}
                </p>
              )}
              <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500">
                {article.author && <span>By {article.author}</span>}
                {article.articleUrl && (
                  <Button variant="link" size="sm" className="px-0" asChild>
                    <a href={article.articleUrl} target="_blank" rel="noopener noreferrer">
                      Read full story
                    </a>
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  };

  const renderAgreementResults = () => {
    if (!agreementResult || !agreementResult.agreements) {
      return <p className="text-sm text-slate-600">Search for agreements to see results.</p>;
    }

    if (!agreementResult.agreements.length) {
      return <p className="text-sm text-slate-600">No agreements found for this country.</p>;
    }

    return (
      <div className="space-y-4">
        {agreementResult.agreements.map((agreement, index) => (
          <Card key={agreement.id ?? index}>
            <CardHeader>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <CardTitle className="text-lg font-semibold">
                    {agreement.title || `Agreement ${index + 1}`}
                  </CardTitle>
                  {agreement.agreementType && (
                    <Badge variant="outline" className="mt-2">
                      {agreement.agreementType}
                    </Badge>
                  )}
                </div>
                {agreement.publishedDate && (
                  <span className="text-xs text-slate-500 whitespace-nowrap">
                    {formatDate(agreement.publishedDate)}
                  </span>
                )}
              </div>
              {agreement.summary && (
                <CardDescription className="mt-2 text-sm text-slate-600">
                  {agreement.summary}
                </CardDescription>
              )}
            </CardHeader>
            <CardContent className="space-y-3">
              {agreement.countries && (
                <div className="flex flex-wrap gap-2">
                  {agreement.countries.map((country) => (
                    <Badge key={country} variant="secondary">
                      {country}
                    </Badge>
                  ))}
                </div>
              )}
              <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500">
                {agreement.effectiveDate && (
                  <span>Effective: {formatDate(agreement.effectiveDate)}</span>
                )}
                {agreement.source && <span>Source: {agreement.source}</span>}
                {agreement.documentUrl && (
                  <Button variant="link" size="sm" className="px-0" asChild>
                    <a href={agreement.documentUrl} target="_blank" rel="noopener noreferrer">
                      View document
                    </a>
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  };

  const renderCombinedResults = () => {
    if (!combinedResult) {
      return <p className="text-sm text-slate-600">Run a combined search to see trade insights.</p>;
    }

    return (
      <div className="space-y-6">
        <div>
          <h3 className="text-lg font-semibold text-slate-900">
            News Highlights {combinedResult.country ? `for ${combinedResult.country}` : ""}
          </h3>
          <p className="text-sm text-slate-600 mb-3">
            {combinedResult.newsSection?.totalCount
              ? `${combinedResult.newsSection.totalCount} related articles found.`
              : "No related news found yet."}
          </p>
          {combinedResult.newsSection?.articles && combinedResult.newsSection.articles.length > 0 ? (
            <div className="space-y-4">
              {combinedResult.newsSection.articles.map((article, index) => (
                <Card key={article.id ?? index}>
                  <CardHeader>
                    <CardTitle className="text-base font-semibold">
                      {article.title || `Article ${index + 1}`}
                    </CardTitle>
                    {article.summary && (
                      <CardDescription className="mt-1 text-sm text-slate-600">
                        {article.summary}
                      </CardDescription>
                    )}
                  </CardHeader>
                  <CardContent className="space-y-2">
                    <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500">
                      {article.source && <span>{article.source}</span>}
                      {article.publishedDate && (
                        <span>{formatDate(article.publishedDate)}</span>
                      )}
                    </div>
                    {article.articleUrl && (
                      <Button variant="link" size="sm" className="px-0" asChild>
                        <a href={article.articleUrl} target="_blank" rel="noopener noreferrer">
                          Open article
                        </a>
                      </Button>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-500">No recent articles matched this query.</p>
          )}
        </div>

        <div>
          <h3 className="text-lg font-semibold text-slate-900">
            Agreements Snapshot {combinedResult.country ? `for ${combinedResult.country}` : ""}
          </h3>
          <p className="text-sm text-slate-600 mb-3">
            {combinedResult.agreementsSection?.totalCount
              ? `${combinedResult.agreementsSection.totalCount} related agreements found.`
              : "No related agreements found yet."}
          </p>
          {combinedResult.agreementsSection?.agreements && combinedResult.agreementsSection.agreements.length > 0 ? (
            <div className="space-y-4">
              {combinedResult.agreementsSection.agreements.map((agreement, index) => (
                <Card key={agreement.id ?? index}>
                  <CardHeader>
                    <CardTitle className="text-base font-semibold">
                      {agreement.title || `Agreement ${index + 1}`}
                    </CardTitle>
                    {agreement.summary && (
                      <CardDescription className="mt-1 text-sm text-slate-600">
                        {agreement.summary}
                      </CardDescription>
                    )}
                  </CardHeader>
                  <CardContent className="space-y-2">
                    {agreement.countries && agreement.countries.length > 0 && (
                      <div className="flex flex-wrap gap-2">
                        {agreement.countries.map((country) => (
                          <Badge key={country} variant="secondary">
                            {country}
                          </Badge>
                        ))}
                      </div>
                    )}
                    <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500">
                      {agreement.agreementType && <span>{agreement.agreementType}</span>}
                      {agreement.effectiveDate && (
                        <span>Effective {formatDate(agreement.effectiveDate)}</span>
                      )}
                      {agreement.source && <span>{agreement.source}</span>}
                    </div>
                    {agreement.documentUrl && (
                      <Button variant="link" size="sm" className="px-0" asChild>
                        <a href={agreement.documentUrl} target="_blank" rel="noopener noreferrer">
                          View details
                        </a>
                      </Button>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-500">No active agreements aligned with this query.</p>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-8">
      <Card>
        <CardHeader>
          <CardTitle className="text-2xl font-semibold text-slate-900">Trade Insights</CardTitle>
          <CardDescription>
            Discover recent trade news, agreements, and combined insights tailored to your search criteria.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="query">Search Query</Label>
              <Input
                id="query"
                placeholder="e.g. electronics exports"
                value={request.query}
                onChange={(event) => updateRequestField("query", event.target.value)}
              />
              <p className="text-xs text-slate-500">
                At least two characters are required for news and combined insights searches.
              </p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="country">Country</Label>
              <Input
                id="country"
                placeholder="e.g. Singapore"
                value={request.country}
                onChange={(event) => updateRequestField("country", event.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="product">Product / Sector</Label>
              <Input
                id="product"
                placeholder="e.g. semiconductors"
                value={request.product}
                onChange={(event) => updateRequestField("product", event.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="agreementType">Agreement Type (optional)</Label>
              <Input
                id="agreementType"
                placeholder="e.g. Free Trade Agreement"
                value={request.agreementType}
                onChange={(event) => updateRequestField("agreementType", event.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="limit">Result Limit</Label>
              <Input
                id="limit"
                type="number"
                min={1}
                max={50}
                value={request.limit}
                onChange={(event) => {
                  const numericValue = Number(event.target.value);
                  updateRequestField("limit", Number.isNaN(numericValue) ? 10 : numericValue);
                }}
              />
            </div>
          </div>

          <div className="mt-6 flex flex-wrap gap-3">
            <Button
              variant={activeView === "news" ? "default" : "outline"}
              onClick={() => fetchView("news")}
              disabled={loadingView !== null}
            >
              {loadingView === "news" ? "Loading news..." : "Search News"}
            </Button>
            <Button
              variant={activeView === "agreements" ? "default" : "outline"}
              onClick={() => fetchView("agreements")}
              disabled={loadingView !== null}
            >
              {loadingView === "agreements" ? "Loading agreements..." : "Search Agreements"}
            </Button>
            <Button
              variant={activeView === "combined" ? "default" : "outline"}
              onClick={() => fetchView("combined")}
              disabled={loadingView !== null}
            >
              {loadingView === "combined" ? "Loading insights..." : "Combined Insights"}
            </Button>
          </div>

          {error && (
            <div className="mt-4 rounded-md bg-red-50 border border-red-200 p-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg
                    className="h-5 w-5 text-red-400"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                    aria-hidden="true"
                  >
                    <path
                      fillRule="evenodd"
                      d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z"
                      clipRule="evenodd"
                    />
                  </svg>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-red-800">Error</h3>
                  <div className="mt-2 text-sm text-red-700">
                    <p>{error}</p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="space-y-6">
        {activeView === "news" && (
          <div className="space-y-4">
            <h3 className="text-xl font-semibold text-slate-900">News Results</h3>
            {renderNewsResults()}
          </div>
        )}
        {activeView === "agreements" && (
          <div className="space-y-4">
            <h3 className="text-xl font-semibold text-slate-900">Agreement Results</h3>
            {renderAgreementResults()}
          </div>
        )}
        {activeView === "combined" && (
          <div className="space-y-4">
            <h3 className="text-xl font-semibold text-slate-900">Combined Insights</h3>
            {renderCombinedResults()}
          </div>
        )}
      </div>
    </div>
  );
}


