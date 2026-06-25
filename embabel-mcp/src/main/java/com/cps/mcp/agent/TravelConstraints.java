package com.cps.mcp.agent;

public class TravelConstraints {
    private final TripDuration duration;
    private final BudgetPreference budgetPreference;
    private final TravelStyle travelStyle;
    private final String startDate; // YYYY-MM-DD or null
    private final String endDate;   // YYYY-MM-DD or null

    /** Full constructor (with dates). */
    public TravelConstraints(TripDuration duration, BudgetPreference budgetPreference,
                             TravelStyle travelStyle, String startDate, String endDate) {
        this.duration = duration;
        this.budgetPreference = budgetPreference;
        this.travelStyle = travelStyle;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /** Backward-compatible constructor (no dates). */
    public TravelConstraints(TripDuration duration, BudgetPreference budgetPreference, TravelStyle travelStyle) {
        this(duration, budgetPreference, travelStyle, null, null);
    }

    public TripDuration duration() {
        return duration;
    }

    public BudgetPreference budgetPreference() {
        return budgetPreference;
    }

    public TravelStyle travelStyle() {
        return travelStyle;
    }

    public String startDate() {
        return startDate;
    }

    public String endDate() {
        return endDate;
    }

    @Override
    public String toString() {
        String dateRange = (startDate != null && endDate != null)
                ? ", Dates: " + startDate + " to " + endDate
                : "";
        return String.format("Duration: %s days, Budget: %s, Style: %s%s",
                duration, budgetPreference, travelStyle, dateRange);
    }
}
