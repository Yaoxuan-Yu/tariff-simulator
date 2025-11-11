
"use client"

import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Button } from "@/components/ui/button"
import { CalendarIcon } from "lucide-react"
import { format } from "date-fns"
import { useState } from "react"

interface DateRangePickerProps {
  dateRange: { start: string; end: string }
  onChange: (range: { start: string; end: string }) => void
}

export function DateRangePicker({ dateRange, onChange }: DateRangePickerProps) {
  const [isOpen, setIsOpen] = useState(false)
  const startDate = dateRange.start ? new Date(dateRange.start) : undefined
  const endDate = dateRange.end ? new Date(dateRange.end) : undefined

  const handleSelect = (type: "start" | "end", date: Date | undefined) => {
    if (!date) return

    const formattedDate = format(date, "yyyy-MM-dd")

    if (type === "start") {
      onChange({ ...dateRange, start: formattedDate })
    } else {
      onChange({ ...dateRange, end: formattedDate })
    }
  }

  return (
    <div className="flex gap-2">
      <Popover>
        <PopoverTrigger asChild>
          <Button variant="outline" className="justify-start text-left font-normal bg-transparent">
            <CalendarIcon className="mr-2 h-4 w-4" />
            {startDate ? format(startDate, "PPP") : "Start date"}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-0" align="start">
          <Calendar mode="single" selected={startDate} onSelect={(date) => handleSelect("start", date)} initialFocus />
        </PopoverContent>
      </Popover>

      <Popover>
        <PopoverTrigger asChild>
          <Button variant="outline" className="justify-start text-left font-normal bg-transparent">
            <CalendarIcon className="mr-2 h-4 w-4" />
            {endDate ? format(endDate, "PPP") : "End date"}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-0" align="start">
          <Calendar mode="single" selected={endDate} onSelect={(date) => handleSelect("end", date)} initialFocus />
        </PopoverContent>
      </Popover>
    </div>
  )
}
