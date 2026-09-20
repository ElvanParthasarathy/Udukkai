import '../../../adippadai/tharavuru/uruvugal.dart';

abstract class PattiyalKalanjiyam {
  Stream<List<PattiyalTharavuru>> watchPattiyalgal();
  Future<List<PattiyalTharavuru>> getPattiyalgal();
  Future<PattiyalTharavuru?> getById(int id);
  
  Future<int> createPattiyal(PattiyalTharavuru tharavuru);
  Future<void> updatePattiyal(int id, PattiyalTharavuru tharavuru);

  Future<void> deletePattiyal(int id);
  Future<void> bulkDeletePattiyalgal(List<int> ids);
  Future<void> restorePattiyal(int id);
  Future<void> deleteAllPattiyalgal();
  
  Stream<List<PattiyalTharavuru>> watchDeletedPattiyalgal();
  Future<List<PattiyalTharavuru>> getDeletedPattiyalgal();
  
  Future<int> purgeExpiredPattiyalgal({int days = 30});

  Future<int> getNextVanakkam(int? niruvanamId, int finYear);
  String formatPattiyalEn(String prefix, int vanakkam);
  Future<bool> isPattiyalEnDuplicate(
    int? niruvanamId,
    int finYear,
    String pattiyalEn, {
    int? excludeId,
  });
}
