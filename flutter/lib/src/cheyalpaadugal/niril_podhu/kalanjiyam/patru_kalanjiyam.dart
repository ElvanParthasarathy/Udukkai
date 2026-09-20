import '../../../adippadai/tharavuru/uruvugal.dart';

abstract class PatruKalanjiyam {
  Stream<List<PatrugalTharavuru>> watchPatrugal();
  Future<List<PatrugalTharavuru>> getPatrugal();
  Future<PatrugalTharavuru?> getById(int id);
  Future<List<PatruPattiyalInaippuTharavuru>> getLinksForPatru(int patruId);
  
  Future<int> insertPatru(PatrugalTharavuru data, List<PatruPattiyalInaippuTharavuru> links);
  Future<void> updatePatru(int id, PatrugalTharavuru data, List<PatruPattiyalInaippuTharavuru> links);

  Future<void> deletePatru(int id);
  Future<void> bulkDeletePatrugal(List<int> ids);
  Future<void> deleteAllPatrugal();

  Stream<double> watchPaidAmount(int pattiyalId);
  Future<double> getPaidAmount(int pattiyalId);
  Stream<List<PattiyalTharavuru>> watchUnpaidInvoices();
  Future<double> getPendingBalance(int pattiyalId);
  Future<Map<int, double>> getPaidAmountsForInvoices(List<int> pattiyalIds);

  Future<int> getNextVanakkam(int? niruvanamId);
  String formatPatruEn(String bizShort, int vanakkam);
  Future<bool> isPatruEnDuplicate(int? niruvanamId, String patruEn, {int? excludeId});
  Future<String?> validateLinks(List<PatruPattiyalInaippuTharavuru> links, {int? excludePatruId});
}
