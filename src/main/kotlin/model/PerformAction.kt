package model

sealed class PerformAction{
    data object RefreshList: PerformAction()
    data class UpgradePackage(val packageName:String): PerformAction()
    data class UninstallPackage(val packageName:String): PerformAction()
}
