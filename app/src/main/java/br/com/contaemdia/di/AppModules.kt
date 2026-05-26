package br.com.contaemdia.di

import androidx.room.Room
import br.com.contaemdia.data.local.database.ContaEmDiaDatabase
import br.com.contaemdia.data.repository.BillRepositoryImpl
import br.com.contaemdia.domain.repository.BillRepository
import br.com.contaemdia.domain.usecase.BuildMonthlySummaryUseCase
import br.com.contaemdia.domain.usecase.DeleteBillUseCase
import br.com.contaemdia.domain.usecase.FilterSortBillsUseCase
import br.com.contaemdia.domain.usecase.MarkBillOpenUseCase
import br.com.contaemdia.domain.usecase.MarkBillPaidUseCase
import br.com.contaemdia.domain.usecase.ObserveBillByIdUseCase
import br.com.contaemdia.domain.usecase.ObserveBillsByMonthUseCase
import br.com.contaemdia.domain.usecase.SaveBillUseCase
import br.com.contaemdia.notification.BillReminderScheduler
import br.com.contaemdia.notification.WorkManagerBillReminderScheduler
import br.com.contaemdia.presentation.bill_detail.BillDetailViewModel
import br.com.contaemdia.presentation.bill_form.BillFormViewModel
import br.com.contaemdia.presentation.dashboard.DashboardViewModel
import br.com.contaemdia.presentation.summary.SummaryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val dataModule = module {
    single {
        Room.databaseBuilder(
            get(),
            ContaEmDiaDatabase::class.java,
            "conta_em_dia.db",
        ).build()
    }
    single { get<ContaEmDiaDatabase>().billDao() }
    single<BillRepository> { BillRepositoryImpl(get()) }
    single<BillReminderScheduler> { WorkManagerBillReminderScheduler(get()) }
}

private val domainModule = module {
    factory { ObserveBillsByMonthUseCase(get()) }
    factory { ObserveBillByIdUseCase(get()) }
    factory { SaveBillUseCase(get()) }
    factory { DeleteBillUseCase(get()) }
    factory { MarkBillPaidUseCase(get()) }
    factory { MarkBillOpenUseCase(get()) }
    factory { BuildMonthlySummaryUseCase() }
    factory { FilterSortBillsUseCase() }
}

private val presentationModule = module {
    viewModel { DashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { parameters -> BillFormViewModel(parameters.get(), get(), get(), get()) }
    viewModel { parameters -> BillDetailViewModel(parameters.get(), get(), get(), get(), get(), get()) }
    viewModel { SummaryViewModel(get(), get()) }
}

val appModules = listOf(dataModule, domainModule, presentationModule)
