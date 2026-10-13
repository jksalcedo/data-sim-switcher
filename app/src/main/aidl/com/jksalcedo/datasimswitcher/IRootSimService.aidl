package com.jksalcedo.datasimswitcher;

interface IRootSimService {
    oneway void switchDataSim();
    String getDisplayName();
}