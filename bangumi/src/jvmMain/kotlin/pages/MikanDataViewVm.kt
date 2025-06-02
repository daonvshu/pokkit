package pages

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Calendar

class MikanDataViewVm : ViewModel() {

    val filterYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val filterSeason = MutableStateFlow(when(Calendar.getInstance().get(Calendar.MONTH)) {
        0,1,2 -> 0
        3,4,5 -> 1
        6,7,8 -> 2
        9,10,11 -> 3
        else -> 0
    })

    val weekDayFilter = MutableStateFlow(
        (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    )
}