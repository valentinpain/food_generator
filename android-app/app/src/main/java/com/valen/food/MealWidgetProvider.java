package com.valen.food;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MealWidgetProvider extends AppWidgetProvider {
    public static final String PREFERENCES = "reserve-widget";
    public static final String PAYLOAD = "payload";
    public static final String ACTION_TOGGLE = "com.valen.food.TOGGLE_MEAL";
    public static final String EXTRA_INDEX = "meal_index";
    public static final String EXTRA_APPWIDGET_ID = "app_widget_id";

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, MealWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        new MealWidgetProvider().onUpdate(context, manager, ids);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) manager.updateAppWidget(id, buildViews(context, id));
    }

    @Override
    public void onEnabled(Context context) {
        android.content.SharedPreferences.Editor editor =
            context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit();
        for (int i = 0; i < 32; i++) editor.remove("expanded_" + i);
        editor.apply();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (!ACTION_TOGGLE.equals(intent.getAction())) return;

        int index = intent.getIntExtra(EXTRA_INDEX, -1);
        int widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        boolean current = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean("expanded_" + index, false);
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit().putBoolean("expanded_" + index, !current).apply();

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            manager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_meals_list);
        } else {
            updateAll(context);
        }
    }

    private RemoteViews buildViews(Context context, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_meals);
        String dateLabel = new SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
                .format(new Date());
        views.setTextViewText(R.id.widget_title, "Repas du " + dateLabel);
        Intent serviceIntent = new Intent(context, MealWidgetService.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        views.setRemoteAdapter(R.id.widget_meals_list, serviceIntent);
        views.setEmptyView(R.id.widget_meals_list, R.id.widget_empty);

        Intent toggleIntent = new Intent(context, MealWidgetProvider.class)
                .setAction(ACTION_TOGGLE);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= 31) flags |= PendingIntent.FLAG_MUTABLE;
        views.setPendingIntentTemplate(R.id.widget_meals_list,
                PendingIntent.getBroadcast(context, widgetId, toggleIntent, flags));
        return views;
    }
}
