package net.arwix.gastro.boss.ui.printers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import net.arwix.gastro.boss.data.printer.Printer
import net.arwix.gastro.boss.data.printer.PrinterRepository
import net.arwix.gastro.boss.data.printer.Printers
import net.arwix.mvi.SimpleIntentViewModel

class PrintersViewModel(private val printerRepository: PrinterRepository) :
    SimpleIntentViewModel<PrintersViewModel.Action, PrintersViewModel.Result, PrintersViewModel.State>() {
    override var internalViewState: State = State()

    init {
        viewModelScope.launch {
            printerRepository.selectPrintersAsFlow()
                .collectIndexed { index, value ->
                    if (index == 0) {
                        notificationFromObserver(
                            Result.Init(
                                printerRepository.getOrUpdatePrinters(),
                                printerRepository.checkSelectedPrinters(value)
                            )
                        )
                    } else {
                        Log.e("change selected", value.toString())
                        notificationFromObserver(Result.ChangeSelectedList(value))
                    }
                }
        }
    }

    override fun dispatchAction(action: Action): LiveData<Result> {
        return liveData {

        }
    }

    override fun reduce(result: Result): State {
        Log.e("reduce", result.toString())
        return when (result) {
            is Result.Init -> {
                internalViewState.copy(
                    selectedPrinters = result.selectedPrinters,
                    printers = result.printers
                )
            }
            is Result.ChangeSelectedList -> {
                internalViewState.copy(
                    selectedPrinters = result.list
                )
            }
        }
    }

    fun submit(list: List<Printer>) {
        printerRepository.setSelectedPrinters(list)
    }

    sealed class Action
    sealed class Result {
        data class ChangeSelectedList(val list: List<Printer>) : Result()
        data class Init(val printers: Printers?, val selectedPrinters: List<Printer>) : Result()
    }

    data class State(val printers: Printers? = null, val selectedPrinters: List<Printer> = listOf())
}