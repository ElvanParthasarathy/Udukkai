import '../../../adippadai/tharavuru/uruvugal.dart';

abstract class PorulKalanjiyam {
  Stream<List<PorulTharavuru>> watchAllPorulgal();
  Future<List<PorulTharavuru>> getAllPorulgal();
  Future<PorulTharavuru?> getPorulById(int id);
  
  Future<int> savePorul(PorulTharavuru tharavuru);

  Future<void> deletePorul(int id);
  Future<void> bulkDeletePorulgal(List<int> ids);
  Future<void> restorePorul(int id);
  Future<void> bulkRestorePorulgal(List<int> ids);
  Future<void> permanentDeletePorul(int id);
  
  Stream<List<PorulTharavuru>> watchDeletedPorulgal();
  Future<int> purgeExpiredPorulgal({int days = 30});
  Future<void> deleteAllPorulgal();
}
