import '../../../adippadai/tharavuru/uruvugal.dart';

abstract class VaangunarKalanjiyam {
  Stream<List<VaangunarTharavuru>> watchAllVaangunargal();
  Future<List<VaangunarTharavuru>> getAllVaangunargal();
  Future<VaangunarTharavuru?> getVaangunarById(int id);
  
  Future<int> saveVaangunar(VaangunarTharavuru tharavuru);

  Future<void> deleteVaangunar(int id);
  Future<void> bulkDeleteVaangunargal(List<int> ids);
  Future<void> restoreVaangunar(int id);
  Future<void> bulkRestoreVaangunargal(List<int> ids);
  Future<void> permanentDeleteVaangunar(int id);
  
  Stream<List<VaangunarTharavuru>> watchDeletedVaangunargal();
  Future<int> purgeExpiredVaangunargal({int days = 30});
  Future<void> deleteAllVaangunargal();
}
