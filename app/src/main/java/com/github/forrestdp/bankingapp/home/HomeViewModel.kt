package com.github.forrestdp.bankingapp.home

import androidx.lifecycle.*
import com.github.forrestdp.bankingapp.network.bankinginfo.CardHoldersApi
import com.github.forrestdp.bankingapp.network.bankinginfo.CardUser
import com.github.forrestdp.bankingapp.network.bankinginfo.Transaction
import com.github.forrestdp.bankingapp.network.currencyinfo.Currency
import com.github.forrestdp.bankingapp.network.currencyinfo.CurrencyInfoApi
import com.github.forrestdp.bankingapp.utils.CurrencyCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode

class HomeViewModel : ViewModel() {
    private val _currentUser = MutableLiveData(CardUser.defaultUser)
    private val _currentCurrency = MutableLiveData(CurrencyCode.USD)

    private lateinit var users: List<CardUser>
    private lateinit var currencies: Map<String, Currency>

    val cardNumber: LiveData<String> = _currentUser.map { it.cardNumber }
    val cardholderName: LiveData<String> = _currentUser.map { it.cardholderName }
    val validThruDate: LiveData<String> = _currentUser.map { it.validThruDate }
    val balance: LiveData<String> = _currentUser.map { "$ ${it.balance.setScale(2, RoundingMode.HALF_UP)}" }
    private val _currencyBalance = MediatorLiveData<String>()
    val currencyBalance: LiveData<String> = _currencyBalance

    private val _transactionHistory = MediatorLiveData<List<Transaction>>()
    val transactionHistory: LiveData<List<Transaction>> = _transactionHistory

    init {
        viewModelScope.launch(Dispatchers.IO) {
            users = CardHoldersApi.retrofitService.getBankingInfo().users
            _currentUser.postValue(users[0])
            currencies = CurrencyInfoApi.retrofitService.getCurrencyInfo().currencies

            withContext(Dispatchers.Main) {
                _currencyBalance.addSource(balance) {
                    renewCurrencyBalance(it!!, _currentCurrency.value!!)
                }
                _currencyBalance.addSource(_currentCurrency) {
                    renewCurrencyBalance(balance.value!!, it!!)
                }

                _transactionHistory.addSource(_currentUser) { user ->
                    _transactionHistory.value = user.transactionHistory.map { it.copy(amount = it.amount.drop(1)) }
                }
                _transactionHistory.addSource(_currentCurrency) { code ->
                    _transactionHistory.value = _transactionHistory.value?.map { transaction ->
                        val currencyCoeff = when (code) {
                            CurrencyCode.USD -> currencies.getValue("USD").value
                            CurrencyCode.GBP -> currencies.getValue("GBP").value
                            CurrencyCode.EUR -> currencies.getValue("EUR").value
                            CurrencyCode.RUB -> 1.0.toBigDecimal()
                            null -> return@addSource
                        }
                        transaction.copy(currencyCode = code,
                            currencyMultiplier = (currencies.getValue("USD").value / currencyCoeff))
                    }
                }
            }
        }
    }

    private fun renewCurrencyBalance(balance: String, code: CurrencyCode) {
        val balanceDouble = balance.drop(2).toBigDecimal()
        val (currencyCoeff, badge) = when (code) {
            CurrencyCode.USD -> currencies.getValue("USD").value to "$"
            CurrencyCode.GBP -> currencies.getValue("GBP").value to "£"
            CurrencyCode.EUR -> currencies.getValue("EUR").value to "€"
            CurrencyCode.RUB -> 1.0.toBigDecimal() to "₽"
        }
        _currencyBalance.value =
            "$badge ${((currencies.getValue("USD").value * balanceDouble) / currencyCoeff).setScale(2, RoundingMode.HALF_UP)}"
    }

    val isGbpChecked: LiveData<Boolean> = _currentCurrency.map { it == CurrencyCode.GBP }
    val isEurChecked: LiveData<Boolean> = _currentCurrency.map { it == CurrencyCode.EUR }
    val isRubChecked: LiveData<Boolean> = _currentCurrency.map { it == CurrencyCode.RUB }

    fun toggleGbp() {
        toggleCurrency(CurrencyCode.GBP)
    }

    fun toggleEur() {
        toggleCurrency(CurrencyCode.EUR)
    }

    fun toggleRub() {
        toggleCurrency(CurrencyCode.RUB)
    }

    private fun toggleCurrency(currencyCode: CurrencyCode) {
        if (_currentCurrency.value == currencyCode) {
            _currentCurrency.value = CurrencyCode.USD
        } else {
            _currentCurrency.value = currencyCode
        }
    }
}