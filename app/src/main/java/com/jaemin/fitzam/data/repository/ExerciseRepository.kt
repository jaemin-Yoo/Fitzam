package com.jaemin.fitzam.data.repository

import com.jaemin.fitzam.data.source.local.dao.ExerciseDao
import javax.inject.Inject

class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
) {

}