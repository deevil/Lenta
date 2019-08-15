<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    tools:context="${packageName}.${className}">

    <data>

        <variable
            name="vm"
            type="${packageName}.${viewModelName}" />

        <#if numberOfTabs != "0">
        <variable
            name="viewPagerSettings"
            type="com.lenta.shared.utilities.databinding.ViewPagerSettings" />
        </#if>

    </data>

    <#if numberOfTabs == "0">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

    </androidx.constraintlayout.widget.ConstraintLayout>    
    
    <#else>
    
    <androidx.viewpager.widget.ViewPager
        android:id="@+id/view_pager"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:pageSelectionListener="@{vm}"
        app:tabPosition="@{vm.selectedPage}"
        app:viewPagerSettings="@{viewPagerSettings}">

        <com.google.android.material.tabs.TabLayout
            android:id="@+id/tab_strip"
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:background="@color/colorBottomPanelBackground"
            android:elevation="0dp"
            android:textSize="15sp"
            app:tabBackground="?selectableItemBackground"
            app:tabIndicatorColor="@color/colorTabIndicator"
            app:tabIndicatorHeight="4dp"
            app:tabMode="scrollable"
            app:tabSelectedTextColor="@color/colorTabIndicator"
            app:tabTextAppearance="@style/TabLayoutStyle"
            app:tabTextColor="@color/colorWhite"
            tools:targetApi="lollipop" />
    </androidx.viewpager.widget.ViewPager>

    </#if>

    
    

</layout>