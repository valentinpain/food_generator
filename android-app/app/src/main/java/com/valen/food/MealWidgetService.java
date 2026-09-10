package com.valen.food;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

public class MealWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MealFactory(intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID));
    }

    private class MealFactory implements RemoteViewsFactory {
        private final int widgetId;
        private JSONArray meals = new JSONArray();

        MealFactory(int widgetId) { this.widgetId = widgetId; }
        @Override public void onCreate() { }

        @Override
        public void onDataSetChanged() {
            String raw = getSharedPreferences(MealWidgetProvider.PREFERENCES, MODE_PRIVATE)
                    .getString(MealWidgetProvider.PAYLOAD, null);
            try {
                JSONObject payload = raw == null ? null : new JSONObject(raw);
                JSONObject days = payload == null ? null : payload.optJSONObject("days");
                if (days != null) {
                    String[] dayKeys = {"", "dim", "lun", "mar", "mer", "jeu", "ven", "sam"};
                    String currentDay = dayKeys[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)];
                    meals = days.optJSONArray(currentDay);
                } else {
                    meals = payload == null ? null : payload.optJSONArray("meals");
                }
                if (meals == null) meals = new JSONArray();
            } catch (Exception ignored) {
                meals = new JSONArray();
            }
        }

        @Override public void onDestroy() { }
        @Override public int getCount() { return meals.length(); }
        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return true; }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews row = new RemoteViews(getPackageName(), R.layout.widget_meal);
            try {
                JSONObject meal = meals.getJSONObject(position);
                row.setTextViewText(R.id.widget_meal_moment, meal.optString("moment", ""));
                row.setTextViewText(R.id.widget_meal_name, meal.optString("name", "Repas"));
                JSONArray ingredients = meal.optJSONArray("ingredients");
                StringBuilder lines = new StringBuilder();
                if (ingredients != null) {
                    for (int i = 0; i < ingredients.length(); i++) {
                        if (i > 0) lines.append("\n");
                        lines.append("• ").append(ingredients.optString(i));
                    }
                }
                row.setTextViewText(R.id.widget_meal_ingredients, lines.toString());
                boolean expanded = getSharedPreferences(MealWidgetProvider.PREFERENCES, MODE_PRIVATE)
                        .getBoolean("expanded_" + position, false);
                row.setViewVisibility(R.id.widget_meal_ingredients,
                        expanded ? android.view.View.VISIBLE : android.view.View.GONE);
                Intent fillIn = new Intent()
                        .putExtra(MealWidgetProvider.EXTRA_INDEX, position)
                        .putExtra(MealWidgetProvider.EXTRA_APPWIDGET_ID, widgetId);
                row.setOnClickFillInIntent(R.id.widget_meal_root, fillIn);
                row.setOnClickFillInIntent(R.id.widget_meal_header, fillIn);
            } catch (Exception ignored) { }
            return row;
        }
    }
}
