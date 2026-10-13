package com.jksalcedo.datasimswitcher;

interface IRootSimService {
    @nullable String switchDataSim();
    String getDisplayName();
}