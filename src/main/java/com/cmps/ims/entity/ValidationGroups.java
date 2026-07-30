package com.cmps.ims.entity;

import jakarta.validation.groups.Default;

public interface ValidationGroups {
    interface OnCreate extends Default {}
    interface OnUpdate extends Default {}
}