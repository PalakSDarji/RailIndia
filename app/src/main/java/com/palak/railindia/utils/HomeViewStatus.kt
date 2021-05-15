package com.palak.railindia.utils

sealed class HomeViewStatus {

    object Empty : HomeViewStatus()
    object ShowList : HomeViewStatus()
    object Searching : HomeViewStatus()
}