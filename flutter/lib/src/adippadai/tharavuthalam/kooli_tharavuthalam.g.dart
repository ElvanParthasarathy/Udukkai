// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'kooli_tharavuthalam.dart';

// ignore_for_file: type=lint
class $KooliNiruvanaTharavugalTableTable extends KooliNiruvanaTharavugalTable
    with
        TableInfo<$KooliNiruvanaTharavugalTableTable,
            KooliNiruvanaTharavugalEntry> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $KooliNiruvanaTharavugalTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      niruvanathinPeyar = GeneratedColumn<String>(
              'niruvanathin_peyar', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliNiruvanaTharavugalTableTable.$converterniruvanathinPeyar);
  static const VerificationMeta _kurumPeyarMeta =
      const VerificationMeta('kurumPeyar');
  @override
  late final GeneratedColumn<String> kurumPeyar = GeneratedColumn<String>(
      'kurum_peyar', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _tholaipaesi1Meta =
      const VerificationMeta('tholaipaesi1');
  @override
  late final GeneratedColumn<String> tholaipaesi1 = GeneratedColumn<String>(
      'tholaipaesi1', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _tholaipaesi2Meta =
      const VerificationMeta('tholaipaesi2');
  @override
  late final GeneratedColumn<String> tholaipaesi2 = GeneratedColumn<String>(
      'tholaipaesi2', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _minnanjalMeta =
      const VerificationMeta('minnanjal');
  @override
  late final GeneratedColumn<String> minnanjal = GeneratedColumn<String>(
      'minnanjal', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _gstinMeta = const VerificationMeta('gstin');
  @override
  late final GeneratedColumn<String> gstin = GeneratedColumn<String>(
      'gstin', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      mugavari = GeneratedColumn<String>('mugavari', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliNiruvanaTharavugalTableTable.$convertermugavari);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String> oor =
      GeneratedColumn<String>('oor', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliNiruvanaTharavugalTableTable.$converteroor);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      maavattam = GeneratedColumn<String>('maavattam', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliNiruvanaTharavugalTableTable.$convertermaavattam);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      maanilam = GeneratedColumn<String>('maanilam', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliNiruvanaTharavugalTableTable.$convertermaanilam);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      naadu = GeneratedColumn<String>('naadu', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue:
                  const Constant('{\"en\": \"India\", \"ta\": \"இந்தியா\"}'))
          .withConverter<Map<String, String>>(
              $KooliNiruvanaTharavugalTableTable.$converternaadu);
  static const VerificationMeta _anjalKuriyeeduMeta =
      const VerificationMeta('anjalKuriyeedu');
  @override
  late final GeneratedColumn<String> anjalKuriyeedu = GeneratedColumn<String>(
      'anjal_kuriyeedu', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      vangiPeyar = GeneratedColumn<String>('vangi_peyar', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliNiruvanaTharavugalTableTable.$convertervangiPeyar);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      kilai = GeneratedColumn<String>('kilai', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliNiruvanaTharavugalTableTable.$converterkilai);
  static const VerificationMeta _vangiKanakkuMeta =
      const VerificationMeta('vangiKanakku');
  @override
  late final GeneratedColumn<String> vangiKanakku = GeneratedColumn<String>(
      'vangi_kanakku', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _ifscMeta = const VerificationMeta('ifsc');
  @override
  late final GeneratedColumn<String> ifsc = GeneratedColumn<String>(
      'ifsc', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _oavuruMeta = const VerificationMeta('oavuru');
  @override
  late final GeneratedColumn<String> oavuru = GeneratedColumn<String>(
      'oavuru', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _agalaOavuruMeta =
      const VerificationMeta('agalaOavuru');
  @override
  late final GeneratedColumn<String> agalaOavuru = GeneratedColumn<String>(
      'agala_oavuru', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _thalaippuVadivuMeta =
      const VerificationMeta('thalaippuVadivu');
  @override
  late final GeneratedColumn<String> thalaippuVadivu = GeneratedColumn<String>(
      'thalaippu_vadivu', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('small'));
  static const VerificationMeta _kaiyoppamMeta =
      const VerificationMeta('kaiyoppam');
  @override
  late final GeneratedColumn<String> kaiyoppam = GeneratedColumn<String>(
      'kaiyoppam', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _oppamPeyarMeta =
      const VerificationMeta('oppamPeyar');
  @override
  late final GeneratedColumn<String> oppamPeyar = GeneratedColumn<String>(
      'oppam_peyar', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      adaimozhi = GeneratedColumn<String>('adaimozhi', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliNiruvanaTharavugalTableTable.$converteradaimozhi);
  static const VerificationMeta _upiIdMeta = const VerificationMeta('upiId');
  @override
  late final GeneratedColumn<String> upiId = GeneratedColumn<String>(
      'upi_id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _thoatraNiramMeta =
      const VerificationMeta('thoatraNiram');
  @override
  late final GeneratedColumn<String> thoatraNiram = GeneratedColumn<String>(
      'thoatra_niram', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _updatedAtMeta =
      const VerificationMeta('updatedAt');
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
      'updated_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _isDeletedMeta =
      const VerificationMeta('isDeleted');
  @override
  late final GeneratedColumn<bool> isDeleted = GeneratedColumn<bool>(
      'is_deleted', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_deleted" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _deletedAtMeta =
      const VerificationMeta('deletedAt');
  @override
  late final GeneratedColumn<DateTime> deletedAt = GeneratedColumn<DateTime>(
      'deleted_at', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        niruvanathinPeyar,
        kurumPeyar,
        tholaipaesi1,
        tholaipaesi2,
        minnanjal,
        gstin,
        mugavari,
        oor,
        maavattam,
        maanilam,
        naadu,
        anjalKuriyeedu,
        vangiPeyar,
        kilai,
        vangiKanakku,
        ifsc,
        oavuru,
        agalaOavuru,
        thalaippuVadivu,
        kaiyoppam,
        oppamPeyar,
        adaimozhi,
        upiId,
        thoatraNiram,
        createdAt,
        updatedAt,
        isDeleted,
        deletedAt
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'kooli_niruvana_tharavugal_table';
  @override
  VerificationContext validateIntegrity(
      Insertable<KooliNiruvanaTharavugalEntry> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('kurum_peyar')) {
      context.handle(
          _kurumPeyarMeta,
          kurumPeyar.isAcceptableOrUnknown(
              data['kurum_peyar']!, _kurumPeyarMeta));
    }
    if (data.containsKey('tholaipaesi1')) {
      context.handle(
          _tholaipaesi1Meta,
          tholaipaesi1.isAcceptableOrUnknown(
              data['tholaipaesi1']!, _tholaipaesi1Meta));
    }
    if (data.containsKey('tholaipaesi2')) {
      context.handle(
          _tholaipaesi2Meta,
          tholaipaesi2.isAcceptableOrUnknown(
              data['tholaipaesi2']!, _tholaipaesi2Meta));
    }
    if (data.containsKey('minnanjal')) {
      context.handle(_minnanjalMeta,
          minnanjal.isAcceptableOrUnknown(data['minnanjal']!, _minnanjalMeta));
    }
    if (data.containsKey('gstin')) {
      context.handle(
          _gstinMeta, gstin.isAcceptableOrUnknown(data['gstin']!, _gstinMeta));
    }
    if (data.containsKey('anjal_kuriyeedu')) {
      context.handle(
          _anjalKuriyeeduMeta,
          anjalKuriyeedu.isAcceptableOrUnknown(
              data['anjal_kuriyeedu']!, _anjalKuriyeeduMeta));
    }
    if (data.containsKey('vangi_kanakku')) {
      context.handle(
          _vangiKanakkuMeta,
          vangiKanakku.isAcceptableOrUnknown(
              data['vangi_kanakku']!, _vangiKanakkuMeta));
    }
    if (data.containsKey('ifsc')) {
      context.handle(
          _ifscMeta, ifsc.isAcceptableOrUnknown(data['ifsc']!, _ifscMeta));
    }
    if (data.containsKey('oavuru')) {
      context.handle(_oavuruMeta,
          oavuru.isAcceptableOrUnknown(data['oavuru']!, _oavuruMeta));
    }
    if (data.containsKey('agala_oavuru')) {
      context.handle(
          _agalaOavuruMeta,
          agalaOavuru.isAcceptableOrUnknown(
              data['agala_oavuru']!, _agalaOavuruMeta));
    }
    if (data.containsKey('thalaippu_vadivu')) {
      context.handle(
          _thalaippuVadivuMeta,
          thalaippuVadivu.isAcceptableOrUnknown(
              data['thalaippu_vadivu']!, _thalaippuVadivuMeta));
    }
    if (data.containsKey('kaiyoppam')) {
      context.handle(_kaiyoppamMeta,
          kaiyoppam.isAcceptableOrUnknown(data['kaiyoppam']!, _kaiyoppamMeta));
    }
    if (data.containsKey('oppam_peyar')) {
      context.handle(
          _oppamPeyarMeta,
          oppamPeyar.isAcceptableOrUnknown(
              data['oppam_peyar']!, _oppamPeyarMeta));
    }
    if (data.containsKey('upi_id')) {
      context.handle(
          _upiIdMeta, upiId.isAcceptableOrUnknown(data['upi_id']!, _upiIdMeta));
    }
    if (data.containsKey('thoatra_niram')) {
      context.handle(
          _thoatraNiramMeta,
          thoatraNiram.isAcceptableOrUnknown(
              data['thoatra_niram']!, _thoatraNiramMeta));
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    if (data.containsKey('updated_at')) {
      context.handle(_updatedAtMeta,
          updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta));
    }
    if (data.containsKey('is_deleted')) {
      context.handle(_isDeletedMeta,
          isDeleted.isAcceptableOrUnknown(data['is_deleted']!, _isDeletedMeta));
    }
    if (data.containsKey('deleted_at')) {
      context.handle(_deletedAtMeta,
          deletedAt.isAcceptableOrUnknown(data['deleted_at']!, _deletedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  KooliNiruvanaTharavugalEntry map(Map<String, dynamic> data,
      {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return KooliNiruvanaTharavugalEntry(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      niruvanathinPeyar: $KooliNiruvanaTharavugalTableTable
          .$converterniruvanathinPeyar
          .fromSql(attachedDatabase.typeMapping.read(DriftSqlType.string,
              data['${effectivePrefix}niruvanathin_peyar'])!),
      kurumPeyar: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}kurum_peyar'])!,
      tholaipaesi1: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}tholaipaesi1'])!,
      tholaipaesi2: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}tholaipaesi2'])!,
      minnanjal: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}minnanjal'])!,
      gstin: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}gstin'])!,
      mugavari: $KooliNiruvanaTharavugalTableTable.$convertermugavari.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}mugavari'])!),
      oor: $KooliNiruvanaTharavugalTableTable.$converteroor.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}oor'])!),
      maavattam: $KooliNiruvanaTharavugalTableTable.$convertermaavattam.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}maavattam'])!),
      maanilam: $KooliNiruvanaTharavugalTableTable.$convertermaanilam.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}maanilam'])!),
      naadu: $KooliNiruvanaTharavugalTableTable.$converternaadu.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}naadu'])!),
      anjalKuriyeedu: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}anjal_kuriyeedu'])!,
      vangiPeyar: $KooliNiruvanaTharavugalTableTable.$convertervangiPeyar
          .fromSql(attachedDatabase.typeMapping.read(
              DriftSqlType.string, data['${effectivePrefix}vangi_peyar'])!),
      kilai: $KooliNiruvanaTharavugalTableTable.$converterkilai.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}kilai'])!),
      vangiKanakku: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}vangi_kanakku'])!,
      ifsc: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}ifsc'])!,
      oavuru: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}oavuru'])!,
      agalaOavuru: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}agala_oavuru'])!,
      thalaippuVadivu: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}thalaippu_vadivu'])!,
      kaiyoppam: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}kaiyoppam'])!,
      oppamPeyar: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}oppam_peyar'])!,
      adaimozhi: $KooliNiruvanaTharavugalTableTable.$converteradaimozhi.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}adaimozhi'])!),
      upiId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}upi_id'])!,
      thoatraNiram: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}thoatra_niram'])!,
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
      updatedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated_at'])!,
      isDeleted: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_deleted'])!,
      deletedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}deleted_at']),
    );
  }

  @override
  $KooliNiruvanaTharavugalTableTable createAlias(String alias) {
    return $KooliNiruvanaTharavugalTableTable(attachedDatabase, alias);
  }

  static TypeConverter<Map<String, String>, String>
      $converterniruvanathinPeyar = const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertermugavari =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $converteroor =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertermaavattam =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertermaanilam =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $converternaadu =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertervangiPeyar =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $converterkilai =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $converteradaimozhi =
      const MozhiMapConverter();
}

class KooliNiruvanaTharavugalEntry extends DataClass
    implements Insertable<KooliNiruvanaTharavugalEntry> {
  final int id;
  final Map<String, String> niruvanathinPeyar;
  final String kurumPeyar;
  final String tholaipaesi1;
  final String tholaipaesi2;
  final String minnanjal;
  final String gstin;
  final Map<String, String> mugavari;
  final Map<String, String> oor;
  final Map<String, String> maavattam;
  final Map<String, String> maanilam;
  final Map<String, String> naadu;
  final String anjalKuriyeedu;
  final Map<String, String> vangiPeyar;
  final Map<String, String> kilai;
  final String vangiKanakku;
  final String ifsc;
  final String oavuru;
  final String agalaOavuru;
  final String thalaippuVadivu;
  final String kaiyoppam;
  final String oppamPeyar;
  final Map<String, String> adaimozhi;
  final String upiId;
  final String thoatraNiram;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isDeleted;
  final DateTime? deletedAt;
  const KooliNiruvanaTharavugalEntry(
      {required this.id,
      required this.niruvanathinPeyar,
      required this.kurumPeyar,
      required this.tholaipaesi1,
      required this.tholaipaesi2,
      required this.minnanjal,
      required this.gstin,
      required this.mugavari,
      required this.oor,
      required this.maavattam,
      required this.maanilam,
      required this.naadu,
      required this.anjalKuriyeedu,
      required this.vangiPeyar,
      required this.kilai,
      required this.vangiKanakku,
      required this.ifsc,
      required this.oavuru,
      required this.agalaOavuru,
      required this.thalaippuVadivu,
      required this.kaiyoppam,
      required this.oppamPeyar,
      required this.adaimozhi,
      required this.upiId,
      required this.thoatraNiram,
      required this.createdAt,
      required this.updatedAt,
      required this.isDeleted,
      this.deletedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    {
      map['niruvanathin_peyar'] = Variable<String>(
          $KooliNiruvanaTharavugalTableTable.$converterniruvanathinPeyar
              .toSql(niruvanathinPeyar));
    }
    map['kurum_peyar'] = Variable<String>(kurumPeyar);
    map['tholaipaesi1'] = Variable<String>(tholaipaesi1);
    map['tholaipaesi2'] = Variable<String>(tholaipaesi2);
    map['minnanjal'] = Variable<String>(minnanjal);
    map['gstin'] = Variable<String>(gstin);
    {
      map['mugavari'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$convertermugavari
          .toSql(mugavari));
    }
    {
      map['oor'] = Variable<String>(
          $KooliNiruvanaTharavugalTableTable.$converteroor.toSql(oor));
    }
    {
      map['maavattam'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$convertermaavattam
          .toSql(maavattam));
    }
    {
      map['maanilam'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$convertermaanilam
          .toSql(maanilam));
    }
    {
      map['naadu'] = Variable<String>(
          $KooliNiruvanaTharavugalTableTable.$converternaadu.toSql(naadu));
    }
    map['anjal_kuriyeedu'] = Variable<String>(anjalKuriyeedu);
    {
      map['vangi_peyar'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$convertervangiPeyar
          .toSql(vangiPeyar));
    }
    {
      map['kilai'] = Variable<String>(
          $KooliNiruvanaTharavugalTableTable.$converterkilai.toSql(kilai));
    }
    map['vangi_kanakku'] = Variable<String>(vangiKanakku);
    map['ifsc'] = Variable<String>(ifsc);
    map['oavuru'] = Variable<String>(oavuru);
    map['agala_oavuru'] = Variable<String>(agalaOavuru);
    map['thalaippu_vadivu'] = Variable<String>(thalaippuVadivu);
    map['kaiyoppam'] = Variable<String>(kaiyoppam);
    map['oppam_peyar'] = Variable<String>(oppamPeyar);
    {
      map['adaimozhi'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$converteradaimozhi
          .toSql(adaimozhi));
    }
    map['upi_id'] = Variable<String>(upiId);
    map['thoatra_niram'] = Variable<String>(thoatraNiram);
    map['created_at'] = Variable<DateTime>(createdAt);
    map['updated_at'] = Variable<DateTime>(updatedAt);
    map['is_deleted'] = Variable<bool>(isDeleted);
    if (!nullToAbsent || deletedAt != null) {
      map['deleted_at'] = Variable<DateTime>(deletedAt);
    }
    return map;
  }

  KooliNiruvanaTharavugalTableCompanion toCompanion(bool nullToAbsent) {
    return KooliNiruvanaTharavugalTableCompanion(
      id: Value(id),
      niruvanathinPeyar: Value(niruvanathinPeyar),
      kurumPeyar: Value(kurumPeyar),
      tholaipaesi1: Value(tholaipaesi1),
      tholaipaesi2: Value(tholaipaesi2),
      minnanjal: Value(minnanjal),
      gstin: Value(gstin),
      mugavari: Value(mugavari),
      oor: Value(oor),
      maavattam: Value(maavattam),
      maanilam: Value(maanilam),
      naadu: Value(naadu),
      anjalKuriyeedu: Value(anjalKuriyeedu),
      vangiPeyar: Value(vangiPeyar),
      kilai: Value(kilai),
      vangiKanakku: Value(vangiKanakku),
      ifsc: Value(ifsc),
      oavuru: Value(oavuru),
      agalaOavuru: Value(agalaOavuru),
      thalaippuVadivu: Value(thalaippuVadivu),
      kaiyoppam: Value(kaiyoppam),
      oppamPeyar: Value(oppamPeyar),
      adaimozhi: Value(adaimozhi),
      upiId: Value(upiId),
      thoatraNiram: Value(thoatraNiram),
      createdAt: Value(createdAt),
      updatedAt: Value(updatedAt),
      isDeleted: Value(isDeleted),
      deletedAt: deletedAt == null && nullToAbsent
          ? const Value.absent()
          : Value(deletedAt),
    );
  }

  factory KooliNiruvanaTharavugalEntry.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return KooliNiruvanaTharavugalEntry(
      id: serializer.fromJson<int>(json['id']),
      niruvanathinPeyar:
          serializer.fromJson<Map<String, String>>(json['niruvanathinPeyar']),
      kurumPeyar: serializer.fromJson<String>(json['kurumPeyar']),
      tholaipaesi1: serializer.fromJson<String>(json['tholaipaesi1']),
      tholaipaesi2: serializer.fromJson<String>(json['tholaipaesi2']),
      minnanjal: serializer.fromJson<String>(json['minnanjal']),
      gstin: serializer.fromJson<String>(json['gstin']),
      mugavari: serializer.fromJson<Map<String, String>>(json['mugavari']),
      oor: serializer.fromJson<Map<String, String>>(json['oor']),
      maavattam: serializer.fromJson<Map<String, String>>(json['maavattam']),
      maanilam: serializer.fromJson<Map<String, String>>(json['maanilam']),
      naadu: serializer.fromJson<Map<String, String>>(json['naadu']),
      anjalKuriyeedu: serializer.fromJson<String>(json['anjalKuriyeedu']),
      vangiPeyar: serializer.fromJson<Map<String, String>>(json['vangiPeyar']),
      kilai: serializer.fromJson<Map<String, String>>(json['kilai']),
      vangiKanakku: serializer.fromJson<String>(json['vangiKanakku']),
      ifsc: serializer.fromJson<String>(json['ifsc']),
      oavuru: serializer.fromJson<String>(json['oavuru']),
      agalaOavuru: serializer.fromJson<String>(json['agalaOavuru']),
      thalaippuVadivu: serializer.fromJson<String>(json['thalaippuVadivu']),
      kaiyoppam: serializer.fromJson<String>(json['kaiyoppam']),
      oppamPeyar: serializer.fromJson<String>(json['oppamPeyar']),
      adaimozhi: serializer.fromJson<Map<String, String>>(json['adaimozhi']),
      upiId: serializer.fromJson<String>(json['upiId']),
      thoatraNiram: serializer.fromJson<String>(json['thoatraNiram']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
      isDeleted: serializer.fromJson<bool>(json['isDeleted']),
      deletedAt: serializer.fromJson<DateTime?>(json['deletedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'niruvanathinPeyar':
          serializer.toJson<Map<String, String>>(niruvanathinPeyar),
      'kurumPeyar': serializer.toJson<String>(kurumPeyar),
      'tholaipaesi1': serializer.toJson<String>(tholaipaesi1),
      'tholaipaesi2': serializer.toJson<String>(tholaipaesi2),
      'minnanjal': serializer.toJson<String>(minnanjal),
      'gstin': serializer.toJson<String>(gstin),
      'mugavari': serializer.toJson<Map<String, String>>(mugavari),
      'oor': serializer.toJson<Map<String, String>>(oor),
      'maavattam': serializer.toJson<Map<String, String>>(maavattam),
      'maanilam': serializer.toJson<Map<String, String>>(maanilam),
      'naadu': serializer.toJson<Map<String, String>>(naadu),
      'anjalKuriyeedu': serializer.toJson<String>(anjalKuriyeedu),
      'vangiPeyar': serializer.toJson<Map<String, String>>(vangiPeyar),
      'kilai': serializer.toJson<Map<String, String>>(kilai),
      'vangiKanakku': serializer.toJson<String>(vangiKanakku),
      'ifsc': serializer.toJson<String>(ifsc),
      'oavuru': serializer.toJson<String>(oavuru),
      'agalaOavuru': serializer.toJson<String>(agalaOavuru),
      'thalaippuVadivu': serializer.toJson<String>(thalaippuVadivu),
      'kaiyoppam': serializer.toJson<String>(kaiyoppam),
      'oppamPeyar': serializer.toJson<String>(oppamPeyar),
      'adaimozhi': serializer.toJson<Map<String, String>>(adaimozhi),
      'upiId': serializer.toJson<String>(upiId),
      'thoatraNiram': serializer.toJson<String>(thoatraNiram),
      'createdAt': serializer.toJson<DateTime>(createdAt),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
      'isDeleted': serializer.toJson<bool>(isDeleted),
      'deletedAt': serializer.toJson<DateTime?>(deletedAt),
    };
  }

  KooliNiruvanaTharavugalEntry copyWith(
          {int? id,
          Map<String, String>? niruvanathinPeyar,
          String? kurumPeyar,
          String? tholaipaesi1,
          String? tholaipaesi2,
          String? minnanjal,
          String? gstin,
          Map<String, String>? mugavari,
          Map<String, String>? oor,
          Map<String, String>? maavattam,
          Map<String, String>? maanilam,
          Map<String, String>? naadu,
          String? anjalKuriyeedu,
          Map<String, String>? vangiPeyar,
          Map<String, String>? kilai,
          String? vangiKanakku,
          String? ifsc,
          String? oavuru,
          String? agalaOavuru,
          String? thalaippuVadivu,
          String? kaiyoppam,
          String? oppamPeyar,
          Map<String, String>? adaimozhi,
          String? upiId,
          String? thoatraNiram,
          DateTime? createdAt,
          DateTime? updatedAt,
          bool? isDeleted,
          Value<DateTime?> deletedAt = const Value.absent()}) =>
      KooliNiruvanaTharavugalEntry(
        id: id ?? this.id,
        niruvanathinPeyar: niruvanathinPeyar ?? this.niruvanathinPeyar,
        kurumPeyar: kurumPeyar ?? this.kurumPeyar,
        tholaipaesi1: tholaipaesi1 ?? this.tholaipaesi1,
        tholaipaesi2: tholaipaesi2 ?? this.tholaipaesi2,
        minnanjal: minnanjal ?? this.minnanjal,
        gstin: gstin ?? this.gstin,
        mugavari: mugavari ?? this.mugavari,
        oor: oor ?? this.oor,
        maavattam: maavattam ?? this.maavattam,
        maanilam: maanilam ?? this.maanilam,
        naadu: naadu ?? this.naadu,
        anjalKuriyeedu: anjalKuriyeedu ?? this.anjalKuriyeedu,
        vangiPeyar: vangiPeyar ?? this.vangiPeyar,
        kilai: kilai ?? this.kilai,
        vangiKanakku: vangiKanakku ?? this.vangiKanakku,
        ifsc: ifsc ?? this.ifsc,
        oavuru: oavuru ?? this.oavuru,
        agalaOavuru: agalaOavuru ?? this.agalaOavuru,
        thalaippuVadivu: thalaippuVadivu ?? this.thalaippuVadivu,
        kaiyoppam: kaiyoppam ?? this.kaiyoppam,
        oppamPeyar: oppamPeyar ?? this.oppamPeyar,
        adaimozhi: adaimozhi ?? this.adaimozhi,
        upiId: upiId ?? this.upiId,
        thoatraNiram: thoatraNiram ?? this.thoatraNiram,
        createdAt: createdAt ?? this.createdAt,
        updatedAt: updatedAt ?? this.updatedAt,
        isDeleted: isDeleted ?? this.isDeleted,
        deletedAt: deletedAt.present ? deletedAt.value : this.deletedAt,
      );
  KooliNiruvanaTharavugalEntry copyWithCompanion(
      KooliNiruvanaTharavugalTableCompanion data) {
    return KooliNiruvanaTharavugalEntry(
      id: data.id.present ? data.id.value : this.id,
      niruvanathinPeyar: data.niruvanathinPeyar.present
          ? data.niruvanathinPeyar.value
          : this.niruvanathinPeyar,
      kurumPeyar:
          data.kurumPeyar.present ? data.kurumPeyar.value : this.kurumPeyar,
      tholaipaesi1: data.tholaipaesi1.present
          ? data.tholaipaesi1.value
          : this.tholaipaesi1,
      tholaipaesi2: data.tholaipaesi2.present
          ? data.tholaipaesi2.value
          : this.tholaipaesi2,
      minnanjal: data.minnanjal.present ? data.minnanjal.value : this.minnanjal,
      gstin: data.gstin.present ? data.gstin.value : this.gstin,
      mugavari: data.mugavari.present ? data.mugavari.value : this.mugavari,
      oor: data.oor.present ? data.oor.value : this.oor,
      maavattam: data.maavattam.present ? data.maavattam.value : this.maavattam,
      maanilam: data.maanilam.present ? data.maanilam.value : this.maanilam,
      naadu: data.naadu.present ? data.naadu.value : this.naadu,
      anjalKuriyeedu: data.anjalKuriyeedu.present
          ? data.anjalKuriyeedu.value
          : this.anjalKuriyeedu,
      vangiPeyar:
          data.vangiPeyar.present ? data.vangiPeyar.value : this.vangiPeyar,
      kilai: data.kilai.present ? data.kilai.value : this.kilai,
      vangiKanakku: data.vangiKanakku.present
          ? data.vangiKanakku.value
          : this.vangiKanakku,
      ifsc: data.ifsc.present ? data.ifsc.value : this.ifsc,
      oavuru: data.oavuru.present ? data.oavuru.value : this.oavuru,
      agalaOavuru:
          data.agalaOavuru.present ? data.agalaOavuru.value : this.agalaOavuru,
      thalaippuVadivu: data.thalaippuVadivu.present
          ? data.thalaippuVadivu.value
          : this.thalaippuVadivu,
      kaiyoppam: data.kaiyoppam.present ? data.kaiyoppam.value : this.kaiyoppam,
      oppamPeyar:
          data.oppamPeyar.present ? data.oppamPeyar.value : this.oppamPeyar,
      adaimozhi: data.adaimozhi.present ? data.adaimozhi.value : this.adaimozhi,
      upiId: data.upiId.present ? data.upiId.value : this.upiId,
      thoatraNiram: data.thoatraNiram.present
          ? data.thoatraNiram.value
          : this.thoatraNiram,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
      isDeleted: data.isDeleted.present ? data.isDeleted.value : this.isDeleted,
      deletedAt: data.deletedAt.present ? data.deletedAt.value : this.deletedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('KooliNiruvanaTharavugalEntry(')
          ..write('id: $id, ')
          ..write('niruvanathinPeyar: $niruvanathinPeyar, ')
          ..write('kurumPeyar: $kurumPeyar, ')
          ..write('tholaipaesi1: $tholaipaesi1, ')
          ..write('tholaipaesi2: $tholaipaesi2, ')
          ..write('minnanjal: $minnanjal, ')
          ..write('gstin: $gstin, ')
          ..write('mugavari: $mugavari, ')
          ..write('oor: $oor, ')
          ..write('maavattam: $maavattam, ')
          ..write('maanilam: $maanilam, ')
          ..write('naadu: $naadu, ')
          ..write('anjalKuriyeedu: $anjalKuriyeedu, ')
          ..write('vangiPeyar: $vangiPeyar, ')
          ..write('kilai: $kilai, ')
          ..write('vangiKanakku: $vangiKanakku, ')
          ..write('ifsc: $ifsc, ')
          ..write('oavuru: $oavuru, ')
          ..write('agalaOavuru: $agalaOavuru, ')
          ..write('thalaippuVadivu: $thalaippuVadivu, ')
          ..write('kaiyoppam: $kaiyoppam, ')
          ..write('oppamPeyar: $oppamPeyar, ')
          ..write('adaimozhi: $adaimozhi, ')
          ..write('upiId: $upiId, ')
          ..write('thoatraNiram: $thoatraNiram, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hashAll([
        id,
        niruvanathinPeyar,
        kurumPeyar,
        tholaipaesi1,
        tholaipaesi2,
        minnanjal,
        gstin,
        mugavari,
        oor,
        maavattam,
        maanilam,
        naadu,
        anjalKuriyeedu,
        vangiPeyar,
        kilai,
        vangiKanakku,
        ifsc,
        oavuru,
        agalaOavuru,
        thalaippuVadivu,
        kaiyoppam,
        oppamPeyar,
        adaimozhi,
        upiId,
        thoatraNiram,
        createdAt,
        updatedAt,
        isDeleted,
        deletedAt
      ]);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is KooliNiruvanaTharavugalEntry &&
          other.id == this.id &&
          other.niruvanathinPeyar == this.niruvanathinPeyar &&
          other.kurumPeyar == this.kurumPeyar &&
          other.tholaipaesi1 == this.tholaipaesi1 &&
          other.tholaipaesi2 == this.tholaipaesi2 &&
          other.minnanjal == this.minnanjal &&
          other.gstin == this.gstin &&
          other.mugavari == this.mugavari &&
          other.oor == this.oor &&
          other.maavattam == this.maavattam &&
          other.maanilam == this.maanilam &&
          other.naadu == this.naadu &&
          other.anjalKuriyeedu == this.anjalKuriyeedu &&
          other.vangiPeyar == this.vangiPeyar &&
          other.kilai == this.kilai &&
          other.vangiKanakku == this.vangiKanakku &&
          other.ifsc == this.ifsc &&
          other.oavuru == this.oavuru &&
          other.agalaOavuru == this.agalaOavuru &&
          other.thalaippuVadivu == this.thalaippuVadivu &&
          other.kaiyoppam == this.kaiyoppam &&
          other.oppamPeyar == this.oppamPeyar &&
          other.adaimozhi == this.adaimozhi &&
          other.upiId == this.upiId &&
          other.thoatraNiram == this.thoatraNiram &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt &&
          other.isDeleted == this.isDeleted &&
          other.deletedAt == this.deletedAt);
}

class KooliNiruvanaTharavugalTableCompanion
    extends UpdateCompanion<KooliNiruvanaTharavugalEntry> {
  final Value<int> id;
  final Value<Map<String, String>> niruvanathinPeyar;
  final Value<String> kurumPeyar;
  final Value<String> tholaipaesi1;
  final Value<String> tholaipaesi2;
  final Value<String> minnanjal;
  final Value<String> gstin;
  final Value<Map<String, String>> mugavari;
  final Value<Map<String, String>> oor;
  final Value<Map<String, String>> maavattam;
  final Value<Map<String, String>> maanilam;
  final Value<Map<String, String>> naadu;
  final Value<String> anjalKuriyeedu;
  final Value<Map<String, String>> vangiPeyar;
  final Value<Map<String, String>> kilai;
  final Value<String> vangiKanakku;
  final Value<String> ifsc;
  final Value<String> oavuru;
  final Value<String> agalaOavuru;
  final Value<String> thalaippuVadivu;
  final Value<String> kaiyoppam;
  final Value<String> oppamPeyar;
  final Value<Map<String, String>> adaimozhi;
  final Value<String> upiId;
  final Value<String> thoatraNiram;
  final Value<DateTime> createdAt;
  final Value<DateTime> updatedAt;
  final Value<bool> isDeleted;
  final Value<DateTime?> deletedAt;
  const KooliNiruvanaTharavugalTableCompanion({
    this.id = const Value.absent(),
    this.niruvanathinPeyar = const Value.absent(),
    this.kurumPeyar = const Value.absent(),
    this.tholaipaesi1 = const Value.absent(),
    this.tholaipaesi2 = const Value.absent(),
    this.minnanjal = const Value.absent(),
    this.gstin = const Value.absent(),
    this.mugavari = const Value.absent(),
    this.oor = const Value.absent(),
    this.maavattam = const Value.absent(),
    this.maanilam = const Value.absent(),
    this.naadu = const Value.absent(),
    this.anjalKuriyeedu = const Value.absent(),
    this.vangiPeyar = const Value.absent(),
    this.kilai = const Value.absent(),
    this.vangiKanakku = const Value.absent(),
    this.ifsc = const Value.absent(),
    this.oavuru = const Value.absent(),
    this.agalaOavuru = const Value.absent(),
    this.thalaippuVadivu = const Value.absent(),
    this.kaiyoppam = const Value.absent(),
    this.oppamPeyar = const Value.absent(),
    this.adaimozhi = const Value.absent(),
    this.upiId = const Value.absent(),
    this.thoatraNiram = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  });
  KooliNiruvanaTharavugalTableCompanion.insert({
    this.id = const Value.absent(),
    this.niruvanathinPeyar = const Value.absent(),
    this.kurumPeyar = const Value.absent(),
    this.tholaipaesi1 = const Value.absent(),
    this.tholaipaesi2 = const Value.absent(),
    this.minnanjal = const Value.absent(),
    this.gstin = const Value.absent(),
    this.mugavari = const Value.absent(),
    this.oor = const Value.absent(),
    this.maavattam = const Value.absent(),
    this.maanilam = const Value.absent(),
    this.naadu = const Value.absent(),
    this.anjalKuriyeedu = const Value.absent(),
    this.vangiPeyar = const Value.absent(),
    this.kilai = const Value.absent(),
    this.vangiKanakku = const Value.absent(),
    this.ifsc = const Value.absent(),
    this.oavuru = const Value.absent(),
    this.agalaOavuru = const Value.absent(),
    this.thalaippuVadivu = const Value.absent(),
    this.kaiyoppam = const Value.absent(),
    this.oppamPeyar = const Value.absent(),
    this.adaimozhi = const Value.absent(),
    this.upiId = const Value.absent(),
    this.thoatraNiram = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  });
  static Insertable<KooliNiruvanaTharavugalEntry> custom({
    Expression<int>? id,
    Expression<String>? niruvanathinPeyar,
    Expression<String>? kurumPeyar,
    Expression<String>? tholaipaesi1,
    Expression<String>? tholaipaesi2,
    Expression<String>? minnanjal,
    Expression<String>? gstin,
    Expression<String>? mugavari,
    Expression<String>? oor,
    Expression<String>? maavattam,
    Expression<String>? maanilam,
    Expression<String>? naadu,
    Expression<String>? anjalKuriyeedu,
    Expression<String>? vangiPeyar,
    Expression<String>? kilai,
    Expression<String>? vangiKanakku,
    Expression<String>? ifsc,
    Expression<String>? oavuru,
    Expression<String>? agalaOavuru,
    Expression<String>? thalaippuVadivu,
    Expression<String>? kaiyoppam,
    Expression<String>? oppamPeyar,
    Expression<String>? adaimozhi,
    Expression<String>? upiId,
    Expression<String>? thoatraNiram,
    Expression<DateTime>? createdAt,
    Expression<DateTime>? updatedAt,
    Expression<bool>? isDeleted,
    Expression<DateTime>? deletedAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (niruvanathinPeyar != null) 'niruvanathin_peyar': niruvanathinPeyar,
      if (kurumPeyar != null) 'kurum_peyar': kurumPeyar,
      if (tholaipaesi1 != null) 'tholaipaesi1': tholaipaesi1,
      if (tholaipaesi2 != null) 'tholaipaesi2': tholaipaesi2,
      if (minnanjal != null) 'minnanjal': minnanjal,
      if (gstin != null) 'gstin': gstin,
      if (mugavari != null) 'mugavari': mugavari,
      if (oor != null) 'oor': oor,
      if (maavattam != null) 'maavattam': maavattam,
      if (maanilam != null) 'maanilam': maanilam,
      if (naadu != null) 'naadu': naadu,
      if (anjalKuriyeedu != null) 'anjal_kuriyeedu': anjalKuriyeedu,
      if (vangiPeyar != null) 'vangi_peyar': vangiPeyar,
      if (kilai != null) 'kilai': kilai,
      if (vangiKanakku != null) 'vangi_kanakku': vangiKanakku,
      if (ifsc != null) 'ifsc': ifsc,
      if (oavuru != null) 'oavuru': oavuru,
      if (agalaOavuru != null) 'agala_oavuru': agalaOavuru,
      if (thalaippuVadivu != null) 'thalaippu_vadivu': thalaippuVadivu,
      if (kaiyoppam != null) 'kaiyoppam': kaiyoppam,
      if (oppamPeyar != null) 'oppam_peyar': oppamPeyar,
      if (adaimozhi != null) 'adaimozhi': adaimozhi,
      if (upiId != null) 'upi_id': upiId,
      if (thoatraNiram != null) 'thoatra_niram': thoatraNiram,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (isDeleted != null) 'is_deleted': isDeleted,
      if (deletedAt != null) 'deleted_at': deletedAt,
    });
  }

  KooliNiruvanaTharavugalTableCompanion copyWith(
      {Value<int>? id,
      Value<Map<String, String>>? niruvanathinPeyar,
      Value<String>? kurumPeyar,
      Value<String>? tholaipaesi1,
      Value<String>? tholaipaesi2,
      Value<String>? minnanjal,
      Value<String>? gstin,
      Value<Map<String, String>>? mugavari,
      Value<Map<String, String>>? oor,
      Value<Map<String, String>>? maavattam,
      Value<Map<String, String>>? maanilam,
      Value<Map<String, String>>? naadu,
      Value<String>? anjalKuriyeedu,
      Value<Map<String, String>>? vangiPeyar,
      Value<Map<String, String>>? kilai,
      Value<String>? vangiKanakku,
      Value<String>? ifsc,
      Value<String>? oavuru,
      Value<String>? agalaOavuru,
      Value<String>? thalaippuVadivu,
      Value<String>? kaiyoppam,
      Value<String>? oppamPeyar,
      Value<Map<String, String>>? adaimozhi,
      Value<String>? upiId,
      Value<String>? thoatraNiram,
      Value<DateTime>? createdAt,
      Value<DateTime>? updatedAt,
      Value<bool>? isDeleted,
      Value<DateTime?>? deletedAt}) {
    return KooliNiruvanaTharavugalTableCompanion(
      id: id ?? this.id,
      niruvanathinPeyar: niruvanathinPeyar ?? this.niruvanathinPeyar,
      kurumPeyar: kurumPeyar ?? this.kurumPeyar,
      tholaipaesi1: tholaipaesi1 ?? this.tholaipaesi1,
      tholaipaesi2: tholaipaesi2 ?? this.tholaipaesi2,
      minnanjal: minnanjal ?? this.minnanjal,
      gstin: gstin ?? this.gstin,
      mugavari: mugavari ?? this.mugavari,
      oor: oor ?? this.oor,
      maavattam: maavattam ?? this.maavattam,
      maanilam: maanilam ?? this.maanilam,
      naadu: naadu ?? this.naadu,
      anjalKuriyeedu: anjalKuriyeedu ?? this.anjalKuriyeedu,
      vangiPeyar: vangiPeyar ?? this.vangiPeyar,
      kilai: kilai ?? this.kilai,
      vangiKanakku: vangiKanakku ?? this.vangiKanakku,
      ifsc: ifsc ?? this.ifsc,
      oavuru: oavuru ?? this.oavuru,
      agalaOavuru: agalaOavuru ?? this.agalaOavuru,
      thalaippuVadivu: thalaippuVadivu ?? this.thalaippuVadivu,
      kaiyoppam: kaiyoppam ?? this.kaiyoppam,
      oppamPeyar: oppamPeyar ?? this.oppamPeyar,
      adaimozhi: adaimozhi ?? this.adaimozhi,
      upiId: upiId ?? this.upiId,
      thoatraNiram: thoatraNiram ?? this.thoatraNiram,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      isDeleted: isDeleted ?? this.isDeleted,
      deletedAt: deletedAt ?? this.deletedAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (niruvanathinPeyar.present) {
      map['niruvanathin_peyar'] = Variable<String>(
          $KooliNiruvanaTharavugalTableTable.$converterniruvanathinPeyar
              .toSql(niruvanathinPeyar.value));
    }
    if (kurumPeyar.present) {
      map['kurum_peyar'] = Variable<String>(kurumPeyar.value);
    }
    if (tholaipaesi1.present) {
      map['tholaipaesi1'] = Variable<String>(tholaipaesi1.value);
    }
    if (tholaipaesi2.present) {
      map['tholaipaesi2'] = Variable<String>(tholaipaesi2.value);
    }
    if (minnanjal.present) {
      map['minnanjal'] = Variable<String>(minnanjal.value);
    }
    if (gstin.present) {
      map['gstin'] = Variable<String>(gstin.value);
    }
    if (mugavari.present) {
      map['mugavari'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$convertermugavari
          .toSql(mugavari.value));
    }
    if (oor.present) {
      map['oor'] = Variable<String>(
          $KooliNiruvanaTharavugalTableTable.$converteroor.toSql(oor.value));
    }
    if (maavattam.present) {
      map['maavattam'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$convertermaavattam
          .toSql(maavattam.value));
    }
    if (maanilam.present) {
      map['maanilam'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$convertermaanilam
          .toSql(maanilam.value));
    }
    if (naadu.present) {
      map['naadu'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$converternaadu
          .toSql(naadu.value));
    }
    if (anjalKuriyeedu.present) {
      map['anjal_kuriyeedu'] = Variable<String>(anjalKuriyeedu.value);
    }
    if (vangiPeyar.present) {
      map['vangi_peyar'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$convertervangiPeyar
          .toSql(vangiPeyar.value));
    }
    if (kilai.present) {
      map['kilai'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$converterkilai
          .toSql(kilai.value));
    }
    if (vangiKanakku.present) {
      map['vangi_kanakku'] = Variable<String>(vangiKanakku.value);
    }
    if (ifsc.present) {
      map['ifsc'] = Variable<String>(ifsc.value);
    }
    if (oavuru.present) {
      map['oavuru'] = Variable<String>(oavuru.value);
    }
    if (agalaOavuru.present) {
      map['agala_oavuru'] = Variable<String>(agalaOavuru.value);
    }
    if (thalaippuVadivu.present) {
      map['thalaippu_vadivu'] = Variable<String>(thalaippuVadivu.value);
    }
    if (kaiyoppam.present) {
      map['kaiyoppam'] = Variable<String>(kaiyoppam.value);
    }
    if (oppamPeyar.present) {
      map['oppam_peyar'] = Variable<String>(oppamPeyar.value);
    }
    if (adaimozhi.present) {
      map['adaimozhi'] = Variable<String>($KooliNiruvanaTharavugalTableTable
          .$converteradaimozhi
          .toSql(adaimozhi.value));
    }
    if (upiId.present) {
      map['upi_id'] = Variable<String>(upiId.value);
    }
    if (thoatraNiram.present) {
      map['thoatra_niram'] = Variable<String>(thoatraNiram.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    if (isDeleted.present) {
      map['is_deleted'] = Variable<bool>(isDeleted.value);
    }
    if (deletedAt.present) {
      map['deleted_at'] = Variable<DateTime>(deletedAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('KooliNiruvanaTharavugalTableCompanion(')
          ..write('id: $id, ')
          ..write('niruvanathinPeyar: $niruvanathinPeyar, ')
          ..write('kurumPeyar: $kurumPeyar, ')
          ..write('tholaipaesi1: $tholaipaesi1, ')
          ..write('tholaipaesi2: $tholaipaesi2, ')
          ..write('minnanjal: $minnanjal, ')
          ..write('gstin: $gstin, ')
          ..write('mugavari: $mugavari, ')
          ..write('oor: $oor, ')
          ..write('maavattam: $maavattam, ')
          ..write('maanilam: $maanilam, ')
          ..write('naadu: $naadu, ')
          ..write('anjalKuriyeedu: $anjalKuriyeedu, ')
          ..write('vangiPeyar: $vangiPeyar, ')
          ..write('kilai: $kilai, ')
          ..write('vangiKanakku: $vangiKanakku, ')
          ..write('ifsc: $ifsc, ')
          ..write('oavuru: $oavuru, ')
          ..write('agalaOavuru: $agalaOavuru, ')
          ..write('thalaippuVadivu: $thalaippuVadivu, ')
          ..write('kaiyoppam: $kaiyoppam, ')
          ..write('oppamPeyar: $oppamPeyar, ')
          ..write('adaimozhi: $adaimozhi, ')
          ..write('upiId: $upiId, ')
          ..write('thoatraNiram: $thoatraNiram, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }
}

class $KooliVaangunarTableTable extends KooliVaangunarTable
    with TableInfo<$KooliVaangunarTableTable, KooliVaangunarEntry> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $KooliVaangunarTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      peyar = GeneratedColumn<String>('peyar', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliVaangunarTableTable.$converterpeyar);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      mugavari = GeneratedColumn<String>('mugavari', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliVaangunarTableTable.$convertermugavari);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String> oor =
      GeneratedColumn<String>('oor', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliVaangunarTableTable.$converteroor);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      maavattam = GeneratedColumn<String>('maavattam', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliVaangunarTableTable.$convertermaavattam);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      maanilam = GeneratedColumn<String>('maanilam', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliVaangunarTableTable.$convertermaanilam);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      naadu = GeneratedColumn<String>('naadu', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue:
                  const Constant('{\"en\": \"India\", \"ta\": \"இந்தியா\"}'))
          .withConverter<Map<String, String>>(
              $KooliVaangunarTableTable.$converternaadu);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      velinaadMugavari = GeneratedColumn<String>(
              'velinaad_mugavari', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliVaangunarTableTable.$convertervelinaadMugavari);
  static const VerificationMeta _anjalKuriyeeduMeta =
      const VerificationMeta('anjalKuriyeedu');
  @override
  late final GeneratedColumn<String> anjalKuriyeedu = GeneratedColumn<String>(
      'anjal_kuriyeedu', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _gstinMeta = const VerificationMeta('gstin');
  @override
  late final GeneratedColumn<String> gstin = GeneratedColumn<String>(
      'gstin', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _minnanjalMeta =
      const VerificationMeta('minnanjal');
  @override
  late final GeneratedColumn<String> minnanjal = GeneratedColumn<String>(
      'minnanjal', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _tholaipaesiMeta =
      const VerificationMeta('tholaipaesi');
  @override
  late final GeneratedColumn<String> tholaipaesi = GeneratedColumn<String>(
      'tholaipaesi', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _updatedAtMeta =
      const VerificationMeta('updatedAt');
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
      'updated_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _isDeletedMeta =
      const VerificationMeta('isDeleted');
  @override
  late final GeneratedColumn<bool> isDeleted = GeneratedColumn<bool>(
      'is_deleted', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_deleted" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _deletedAtMeta =
      const VerificationMeta('deletedAt');
  @override
  late final GeneratedColumn<DateTime> deletedAt = GeneratedColumn<DateTime>(
      'deleted_at', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        peyar,
        mugavari,
        oor,
        maavattam,
        maanilam,
        naadu,
        velinaadMugavari,
        anjalKuriyeedu,
        gstin,
        minnanjal,
        tholaipaesi,
        createdAt,
        updatedAt,
        isDeleted,
        deletedAt
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'kooli_vaangunar_table';
  @override
  VerificationContext validateIntegrity(
      Insertable<KooliVaangunarEntry> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('anjal_kuriyeedu')) {
      context.handle(
          _anjalKuriyeeduMeta,
          anjalKuriyeedu.isAcceptableOrUnknown(
              data['anjal_kuriyeedu']!, _anjalKuriyeeduMeta));
    }
    if (data.containsKey('gstin')) {
      context.handle(
          _gstinMeta, gstin.isAcceptableOrUnknown(data['gstin']!, _gstinMeta));
    }
    if (data.containsKey('minnanjal')) {
      context.handle(_minnanjalMeta,
          minnanjal.isAcceptableOrUnknown(data['minnanjal']!, _minnanjalMeta));
    }
    if (data.containsKey('tholaipaesi')) {
      context.handle(
          _tholaipaesiMeta,
          tholaipaesi.isAcceptableOrUnknown(
              data['tholaipaesi']!, _tholaipaesiMeta));
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    if (data.containsKey('updated_at')) {
      context.handle(_updatedAtMeta,
          updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta));
    }
    if (data.containsKey('is_deleted')) {
      context.handle(_isDeletedMeta,
          isDeleted.isAcceptableOrUnknown(data['is_deleted']!, _isDeletedMeta));
    }
    if (data.containsKey('deleted_at')) {
      context.handle(_deletedAtMeta,
          deletedAt.isAcceptableOrUnknown(data['deleted_at']!, _deletedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  KooliVaangunarEntry map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return KooliVaangunarEntry(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      peyar: $KooliVaangunarTableTable.$converterpeyar.fromSql(attachedDatabase
          .typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}peyar'])!),
      mugavari: $KooliVaangunarTableTable.$convertermugavari.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}mugavari'])!),
      oor: $KooliVaangunarTableTable.$converteroor.fromSql(attachedDatabase
          .typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}oor'])!),
      maavattam: $KooliVaangunarTableTable.$convertermaavattam.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}maavattam'])!),
      maanilam: $KooliVaangunarTableTable.$convertermaanilam.fromSql(
          attachedDatabase.typeMapping
              .read(DriftSqlType.string, data['${effectivePrefix}maanilam'])!),
      naadu: $KooliVaangunarTableTable.$converternaadu.fromSql(attachedDatabase
          .typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}naadu'])!),
      velinaadMugavari: $KooliVaangunarTableTable.$convertervelinaadMugavari
          .fromSql(attachedDatabase.typeMapping.read(DriftSqlType.string,
              data['${effectivePrefix}velinaad_mugavari'])!),
      anjalKuriyeedu: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}anjal_kuriyeedu'])!,
      gstin: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}gstin'])!,
      minnanjal: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}minnanjal'])!,
      tholaipaesi: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}tholaipaesi'])!,
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
      updatedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated_at'])!,
      isDeleted: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_deleted'])!,
      deletedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}deleted_at']),
    );
  }

  @override
  $KooliVaangunarTableTable createAlias(String alias) {
    return $KooliVaangunarTableTable(attachedDatabase, alias);
  }

  static TypeConverter<Map<String, String>, String> $converterpeyar =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertermugavari =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $converteroor =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertermaavattam =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertermaanilam =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $converternaadu =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertervelinaadMugavari =
      const MozhiMapConverter();
}

class KooliVaangunarEntry extends DataClass
    implements Insertable<KooliVaangunarEntry> {
  final int id;
  final Map<String, String> peyar;
  final Map<String, String> mugavari;
  final Map<String, String> oor;
  final Map<String, String> maavattam;
  final Map<String, String> maanilam;
  final Map<String, String> naadu;
  final Map<String, String> velinaadMugavari;
  final String anjalKuriyeedu;
  final String gstin;
  final String minnanjal;
  final String tholaipaesi;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isDeleted;
  final DateTime? deletedAt;
  const KooliVaangunarEntry(
      {required this.id,
      required this.peyar,
      required this.mugavari,
      required this.oor,
      required this.maavattam,
      required this.maanilam,
      required this.naadu,
      required this.velinaadMugavari,
      required this.anjalKuriyeedu,
      required this.gstin,
      required this.minnanjal,
      required this.tholaipaesi,
      required this.createdAt,
      required this.updatedAt,
      required this.isDeleted,
      this.deletedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    {
      map['peyar'] = Variable<String>(
          $KooliVaangunarTableTable.$converterpeyar.toSql(peyar));
    }
    {
      map['mugavari'] = Variable<String>(
          $KooliVaangunarTableTable.$convertermugavari.toSql(mugavari));
    }
    {
      map['oor'] =
          Variable<String>($KooliVaangunarTableTable.$converteroor.toSql(oor));
    }
    {
      map['maavattam'] = Variable<String>(
          $KooliVaangunarTableTable.$convertermaavattam.toSql(maavattam));
    }
    {
      map['maanilam'] = Variable<String>(
          $KooliVaangunarTableTable.$convertermaanilam.toSql(maanilam));
    }
    {
      map['naadu'] = Variable<String>(
          $KooliVaangunarTableTable.$converternaadu.toSql(naadu));
    }
    {
      map['velinaad_mugavari'] = Variable<String>($KooliVaangunarTableTable
          .$convertervelinaadMugavari
          .toSql(velinaadMugavari));
    }
    map['anjal_kuriyeedu'] = Variable<String>(anjalKuriyeedu);
    map['gstin'] = Variable<String>(gstin);
    map['minnanjal'] = Variable<String>(minnanjal);
    map['tholaipaesi'] = Variable<String>(tholaipaesi);
    map['created_at'] = Variable<DateTime>(createdAt);
    map['updated_at'] = Variable<DateTime>(updatedAt);
    map['is_deleted'] = Variable<bool>(isDeleted);
    if (!nullToAbsent || deletedAt != null) {
      map['deleted_at'] = Variable<DateTime>(deletedAt);
    }
    return map;
  }

  KooliVaangunarTableCompanion toCompanion(bool nullToAbsent) {
    return KooliVaangunarTableCompanion(
      id: Value(id),
      peyar: Value(peyar),
      mugavari: Value(mugavari),
      oor: Value(oor),
      maavattam: Value(maavattam),
      maanilam: Value(maanilam),
      naadu: Value(naadu),
      velinaadMugavari: Value(velinaadMugavari),
      anjalKuriyeedu: Value(anjalKuriyeedu),
      gstin: Value(gstin),
      minnanjal: Value(minnanjal),
      tholaipaesi: Value(tholaipaesi),
      createdAt: Value(createdAt),
      updatedAt: Value(updatedAt),
      isDeleted: Value(isDeleted),
      deletedAt: deletedAt == null && nullToAbsent
          ? const Value.absent()
          : Value(deletedAt),
    );
  }

  factory KooliVaangunarEntry.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return KooliVaangunarEntry(
      id: serializer.fromJson<int>(json['id']),
      peyar: serializer.fromJson<Map<String, String>>(json['peyar']),
      mugavari: serializer.fromJson<Map<String, String>>(json['mugavari']),
      oor: serializer.fromJson<Map<String, String>>(json['oor']),
      maavattam: serializer.fromJson<Map<String, String>>(json['maavattam']),
      maanilam: serializer.fromJson<Map<String, String>>(json['maanilam']),
      naadu: serializer.fromJson<Map<String, String>>(json['naadu']),
      velinaadMugavari:
          serializer.fromJson<Map<String, String>>(json['velinaadMugavari']),
      anjalKuriyeedu: serializer.fromJson<String>(json['anjalKuriyeedu']),
      gstin: serializer.fromJson<String>(json['gstin']),
      minnanjal: serializer.fromJson<String>(json['minnanjal']),
      tholaipaesi: serializer.fromJson<String>(json['tholaipaesi']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
      isDeleted: serializer.fromJson<bool>(json['isDeleted']),
      deletedAt: serializer.fromJson<DateTime?>(json['deletedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'peyar': serializer.toJson<Map<String, String>>(peyar),
      'mugavari': serializer.toJson<Map<String, String>>(mugavari),
      'oor': serializer.toJson<Map<String, String>>(oor),
      'maavattam': serializer.toJson<Map<String, String>>(maavattam),
      'maanilam': serializer.toJson<Map<String, String>>(maanilam),
      'naadu': serializer.toJson<Map<String, String>>(naadu),
      'velinaadMugavari':
          serializer.toJson<Map<String, String>>(velinaadMugavari),
      'anjalKuriyeedu': serializer.toJson<String>(anjalKuriyeedu),
      'gstin': serializer.toJson<String>(gstin),
      'minnanjal': serializer.toJson<String>(minnanjal),
      'tholaipaesi': serializer.toJson<String>(tholaipaesi),
      'createdAt': serializer.toJson<DateTime>(createdAt),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
      'isDeleted': serializer.toJson<bool>(isDeleted),
      'deletedAt': serializer.toJson<DateTime?>(deletedAt),
    };
  }

  KooliVaangunarEntry copyWith(
          {int? id,
          Map<String, String>? peyar,
          Map<String, String>? mugavari,
          Map<String, String>? oor,
          Map<String, String>? maavattam,
          Map<String, String>? maanilam,
          Map<String, String>? naadu,
          Map<String, String>? velinaadMugavari,
          String? anjalKuriyeedu,
          String? gstin,
          String? minnanjal,
          String? tholaipaesi,
          DateTime? createdAt,
          DateTime? updatedAt,
          bool? isDeleted,
          Value<DateTime?> deletedAt = const Value.absent()}) =>
      KooliVaangunarEntry(
        id: id ?? this.id,
        peyar: peyar ?? this.peyar,
        mugavari: mugavari ?? this.mugavari,
        oor: oor ?? this.oor,
        maavattam: maavattam ?? this.maavattam,
        maanilam: maanilam ?? this.maanilam,
        naadu: naadu ?? this.naadu,
        velinaadMugavari: velinaadMugavari ?? this.velinaadMugavari,
        anjalKuriyeedu: anjalKuriyeedu ?? this.anjalKuriyeedu,
        gstin: gstin ?? this.gstin,
        minnanjal: minnanjal ?? this.minnanjal,
        tholaipaesi: tholaipaesi ?? this.tholaipaesi,
        createdAt: createdAt ?? this.createdAt,
        updatedAt: updatedAt ?? this.updatedAt,
        isDeleted: isDeleted ?? this.isDeleted,
        deletedAt: deletedAt.present ? deletedAt.value : this.deletedAt,
      );
  KooliVaangunarEntry copyWithCompanion(KooliVaangunarTableCompanion data) {
    return KooliVaangunarEntry(
      id: data.id.present ? data.id.value : this.id,
      peyar: data.peyar.present ? data.peyar.value : this.peyar,
      mugavari: data.mugavari.present ? data.mugavari.value : this.mugavari,
      oor: data.oor.present ? data.oor.value : this.oor,
      maavattam: data.maavattam.present ? data.maavattam.value : this.maavattam,
      maanilam: data.maanilam.present ? data.maanilam.value : this.maanilam,
      naadu: data.naadu.present ? data.naadu.value : this.naadu,
      velinaadMugavari: data.velinaadMugavari.present
          ? data.velinaadMugavari.value
          : this.velinaadMugavari,
      anjalKuriyeedu: data.anjalKuriyeedu.present
          ? data.anjalKuriyeedu.value
          : this.anjalKuriyeedu,
      gstin: data.gstin.present ? data.gstin.value : this.gstin,
      minnanjal: data.minnanjal.present ? data.minnanjal.value : this.minnanjal,
      tholaipaesi:
          data.tholaipaesi.present ? data.tholaipaesi.value : this.tholaipaesi,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
      isDeleted: data.isDeleted.present ? data.isDeleted.value : this.isDeleted,
      deletedAt: data.deletedAt.present ? data.deletedAt.value : this.deletedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('KooliVaangunarEntry(')
          ..write('id: $id, ')
          ..write('peyar: $peyar, ')
          ..write('mugavari: $mugavari, ')
          ..write('oor: $oor, ')
          ..write('maavattam: $maavattam, ')
          ..write('maanilam: $maanilam, ')
          ..write('naadu: $naadu, ')
          ..write('velinaadMugavari: $velinaadMugavari, ')
          ..write('anjalKuriyeedu: $anjalKuriyeedu, ')
          ..write('gstin: $gstin, ')
          ..write('minnanjal: $minnanjal, ')
          ..write('tholaipaesi: $tholaipaesi, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id,
      peyar,
      mugavari,
      oor,
      maavattam,
      maanilam,
      naadu,
      velinaadMugavari,
      anjalKuriyeedu,
      gstin,
      minnanjal,
      tholaipaesi,
      createdAt,
      updatedAt,
      isDeleted,
      deletedAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is KooliVaangunarEntry &&
          other.id == this.id &&
          other.peyar == this.peyar &&
          other.mugavari == this.mugavari &&
          other.oor == this.oor &&
          other.maavattam == this.maavattam &&
          other.maanilam == this.maanilam &&
          other.naadu == this.naadu &&
          other.velinaadMugavari == this.velinaadMugavari &&
          other.anjalKuriyeedu == this.anjalKuriyeedu &&
          other.gstin == this.gstin &&
          other.minnanjal == this.minnanjal &&
          other.tholaipaesi == this.tholaipaesi &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt &&
          other.isDeleted == this.isDeleted &&
          other.deletedAt == this.deletedAt);
}

class KooliVaangunarTableCompanion
    extends UpdateCompanion<KooliVaangunarEntry> {
  final Value<int> id;
  final Value<Map<String, String>> peyar;
  final Value<Map<String, String>> mugavari;
  final Value<Map<String, String>> oor;
  final Value<Map<String, String>> maavattam;
  final Value<Map<String, String>> maanilam;
  final Value<Map<String, String>> naadu;
  final Value<Map<String, String>> velinaadMugavari;
  final Value<String> anjalKuriyeedu;
  final Value<String> gstin;
  final Value<String> minnanjal;
  final Value<String> tholaipaesi;
  final Value<DateTime> createdAt;
  final Value<DateTime> updatedAt;
  final Value<bool> isDeleted;
  final Value<DateTime?> deletedAt;
  const KooliVaangunarTableCompanion({
    this.id = const Value.absent(),
    this.peyar = const Value.absent(),
    this.mugavari = const Value.absent(),
    this.oor = const Value.absent(),
    this.maavattam = const Value.absent(),
    this.maanilam = const Value.absent(),
    this.naadu = const Value.absent(),
    this.velinaadMugavari = const Value.absent(),
    this.anjalKuriyeedu = const Value.absent(),
    this.gstin = const Value.absent(),
    this.minnanjal = const Value.absent(),
    this.tholaipaesi = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  });
  KooliVaangunarTableCompanion.insert({
    this.id = const Value.absent(),
    this.peyar = const Value.absent(),
    this.mugavari = const Value.absent(),
    this.oor = const Value.absent(),
    this.maavattam = const Value.absent(),
    this.maanilam = const Value.absent(),
    this.naadu = const Value.absent(),
    this.velinaadMugavari = const Value.absent(),
    this.anjalKuriyeedu = const Value.absent(),
    this.gstin = const Value.absent(),
    this.minnanjal = const Value.absent(),
    this.tholaipaesi = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  });
  static Insertable<KooliVaangunarEntry> custom({
    Expression<int>? id,
    Expression<String>? peyar,
    Expression<String>? mugavari,
    Expression<String>? oor,
    Expression<String>? maavattam,
    Expression<String>? maanilam,
    Expression<String>? naadu,
    Expression<String>? velinaadMugavari,
    Expression<String>? anjalKuriyeedu,
    Expression<String>? gstin,
    Expression<String>? minnanjal,
    Expression<String>? tholaipaesi,
    Expression<DateTime>? createdAt,
    Expression<DateTime>? updatedAt,
    Expression<bool>? isDeleted,
    Expression<DateTime>? deletedAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (peyar != null) 'peyar': peyar,
      if (mugavari != null) 'mugavari': mugavari,
      if (oor != null) 'oor': oor,
      if (maavattam != null) 'maavattam': maavattam,
      if (maanilam != null) 'maanilam': maanilam,
      if (naadu != null) 'naadu': naadu,
      if (velinaadMugavari != null) 'velinaad_mugavari': velinaadMugavari,
      if (anjalKuriyeedu != null) 'anjal_kuriyeedu': anjalKuriyeedu,
      if (gstin != null) 'gstin': gstin,
      if (minnanjal != null) 'minnanjal': minnanjal,
      if (tholaipaesi != null) 'tholaipaesi': tholaipaesi,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (isDeleted != null) 'is_deleted': isDeleted,
      if (deletedAt != null) 'deleted_at': deletedAt,
    });
  }

  KooliVaangunarTableCompanion copyWith(
      {Value<int>? id,
      Value<Map<String, String>>? peyar,
      Value<Map<String, String>>? mugavari,
      Value<Map<String, String>>? oor,
      Value<Map<String, String>>? maavattam,
      Value<Map<String, String>>? maanilam,
      Value<Map<String, String>>? naadu,
      Value<Map<String, String>>? velinaadMugavari,
      Value<String>? anjalKuriyeedu,
      Value<String>? gstin,
      Value<String>? minnanjal,
      Value<String>? tholaipaesi,
      Value<DateTime>? createdAt,
      Value<DateTime>? updatedAt,
      Value<bool>? isDeleted,
      Value<DateTime?>? deletedAt}) {
    return KooliVaangunarTableCompanion(
      id: id ?? this.id,
      peyar: peyar ?? this.peyar,
      mugavari: mugavari ?? this.mugavari,
      oor: oor ?? this.oor,
      maavattam: maavattam ?? this.maavattam,
      maanilam: maanilam ?? this.maanilam,
      naadu: naadu ?? this.naadu,
      velinaadMugavari: velinaadMugavari ?? this.velinaadMugavari,
      anjalKuriyeedu: anjalKuriyeedu ?? this.anjalKuriyeedu,
      gstin: gstin ?? this.gstin,
      minnanjal: minnanjal ?? this.minnanjal,
      tholaipaesi: tholaipaesi ?? this.tholaipaesi,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      isDeleted: isDeleted ?? this.isDeleted,
      deletedAt: deletedAt ?? this.deletedAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (peyar.present) {
      map['peyar'] = Variable<String>(
          $KooliVaangunarTableTable.$converterpeyar.toSql(peyar.value));
    }
    if (mugavari.present) {
      map['mugavari'] = Variable<String>(
          $KooliVaangunarTableTable.$convertermugavari.toSql(mugavari.value));
    }
    if (oor.present) {
      map['oor'] = Variable<String>(
          $KooliVaangunarTableTable.$converteroor.toSql(oor.value));
    }
    if (maavattam.present) {
      map['maavattam'] = Variable<String>(
          $KooliVaangunarTableTable.$convertermaavattam.toSql(maavattam.value));
    }
    if (maanilam.present) {
      map['maanilam'] = Variable<String>(
          $KooliVaangunarTableTable.$convertermaanilam.toSql(maanilam.value));
    }
    if (naadu.present) {
      map['naadu'] = Variable<String>(
          $KooliVaangunarTableTable.$converternaadu.toSql(naadu.value));
    }
    if (velinaadMugavari.present) {
      map['velinaad_mugavari'] = Variable<String>($KooliVaangunarTableTable
          .$convertervelinaadMugavari
          .toSql(velinaadMugavari.value));
    }
    if (anjalKuriyeedu.present) {
      map['anjal_kuriyeedu'] = Variable<String>(anjalKuriyeedu.value);
    }
    if (gstin.present) {
      map['gstin'] = Variable<String>(gstin.value);
    }
    if (minnanjal.present) {
      map['minnanjal'] = Variable<String>(minnanjal.value);
    }
    if (tholaipaesi.present) {
      map['tholaipaesi'] = Variable<String>(tholaipaesi.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    if (isDeleted.present) {
      map['is_deleted'] = Variable<bool>(isDeleted.value);
    }
    if (deletedAt.present) {
      map['deleted_at'] = Variable<DateTime>(deletedAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('KooliVaangunarTableCompanion(')
          ..write('id: $id, ')
          ..write('peyar: $peyar, ')
          ..write('mugavari: $mugavari, ')
          ..write('oor: $oor, ')
          ..write('maavattam: $maavattam, ')
          ..write('maanilam: $maanilam, ')
          ..write('naadu: $naadu, ')
          ..write('velinaadMugavari: $velinaadMugavari, ')
          ..write('anjalKuriyeedu: $anjalKuriyeedu, ')
          ..write('gstin: $gstin, ')
          ..write('minnanjal: $minnanjal, ')
          ..write('tholaipaesi: $tholaipaesi, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }
}

class $KooliPorulTableTable extends KooliPorulTable
    with TableInfo<$KooliPorulTableTable, KooliPorulEntry> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $KooliPorulTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      porulPeyar = GeneratedColumn<String>('porul_peyar', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliPorulTableTable.$converterporulPeyar);
  static const VerificationMeta _hsnCodeMeta =
      const VerificationMeta('hsnCode');
  @override
  late final GeneratedColumn<String> hsnCode = GeneratedColumn<String>(
      'hsn_code', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _vilaiMeta = const VerificationMeta('vilai');
  @override
  late final GeneratedColumn<double> vilai = GeneratedColumn<double>(
      'vilai', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _variVeethamMeta =
      const VerificationMeta('variVeetham');
  @override
  late final GeneratedColumn<double> variVeetham = GeneratedColumn<double>(
      'vari_veetham', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _alavuVagaiMeta =
      const VerificationMeta('alavuVagai');
  @override
  late final GeneratedColumn<String> alavuVagai = GeneratedColumn<String>(
      'alavu_vagai', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('quantity'));
  static const VerificationMeta _alaguMeta = const VerificationMeta('alagu');
  @override
  late final GeneratedColumn<String> alagu = GeneratedColumn<String>(
      'alagu', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('Nos'));
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _updatedAtMeta =
      const VerificationMeta('updatedAt');
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
      'updated_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _isDeletedMeta =
      const VerificationMeta('isDeleted');
  @override
  late final GeneratedColumn<bool> isDeleted = GeneratedColumn<bool>(
      'is_deleted', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_deleted" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _deletedAtMeta =
      const VerificationMeta('deletedAt');
  @override
  late final GeneratedColumn<DateTime> deletedAt = GeneratedColumn<DateTime>(
      'deleted_at', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        porulPeyar,
        hsnCode,
        vilai,
        variVeetham,
        alavuVagai,
        alagu,
        createdAt,
        updatedAt,
        isDeleted,
        deletedAt
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'kooli_porul_table';
  @override
  VerificationContext validateIntegrity(Insertable<KooliPorulEntry> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('hsn_code')) {
      context.handle(_hsnCodeMeta,
          hsnCode.isAcceptableOrUnknown(data['hsn_code']!, _hsnCodeMeta));
    }
    if (data.containsKey('vilai')) {
      context.handle(
          _vilaiMeta, vilai.isAcceptableOrUnknown(data['vilai']!, _vilaiMeta));
    }
    if (data.containsKey('vari_veetham')) {
      context.handle(
          _variVeethamMeta,
          variVeetham.isAcceptableOrUnknown(
              data['vari_veetham']!, _variVeethamMeta));
    }
    if (data.containsKey('alavu_vagai')) {
      context.handle(
          _alavuVagaiMeta,
          alavuVagai.isAcceptableOrUnknown(
              data['alavu_vagai']!, _alavuVagaiMeta));
    }
    if (data.containsKey('alagu')) {
      context.handle(
          _alaguMeta, alagu.isAcceptableOrUnknown(data['alagu']!, _alaguMeta));
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    if (data.containsKey('updated_at')) {
      context.handle(_updatedAtMeta,
          updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta));
    }
    if (data.containsKey('is_deleted')) {
      context.handle(_isDeletedMeta,
          isDeleted.isAcceptableOrUnknown(data['is_deleted']!, _isDeletedMeta));
    }
    if (data.containsKey('deleted_at')) {
      context.handle(_deletedAtMeta,
          deletedAt.isAcceptableOrUnknown(data['deleted_at']!, _deletedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  KooliPorulEntry map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return KooliPorulEntry(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      porulPeyar: $KooliPorulTableTable.$converterporulPeyar.fromSql(
          attachedDatabase.typeMapping.read(
              DriftSqlType.string, data['${effectivePrefix}porul_peyar'])!),
      hsnCode: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}hsn_code'])!,
      vilai: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}vilai'])!,
      variVeetham: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}vari_veetham'])!,
      alavuVagai: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}alavu_vagai'])!,
      alagu: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}alagu'])!,
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
      updatedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated_at'])!,
      isDeleted: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_deleted'])!,
      deletedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}deleted_at']),
    );
  }

  @override
  $KooliPorulTableTable createAlias(String alias) {
    return $KooliPorulTableTable(attachedDatabase, alias);
  }

  static TypeConverter<Map<String, String>, String> $converterporulPeyar =
      const MozhiMapConverter();
}

class KooliPorulEntry extends DataClass implements Insertable<KooliPorulEntry> {
  final int id;
  final Map<String, String> porulPeyar;
  final String hsnCode;
  final double vilai;
  final double variVeetham;
  final String alavuVagai;
  final String alagu;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isDeleted;
  final DateTime? deletedAt;
  const KooliPorulEntry(
      {required this.id,
      required this.porulPeyar,
      required this.hsnCode,
      required this.vilai,
      required this.variVeetham,
      required this.alavuVagai,
      required this.alagu,
      required this.createdAt,
      required this.updatedAt,
      required this.isDeleted,
      this.deletedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    {
      map['porul_peyar'] = Variable<String>(
          $KooliPorulTableTable.$converterporulPeyar.toSql(porulPeyar));
    }
    map['hsn_code'] = Variable<String>(hsnCode);
    map['vilai'] = Variable<double>(vilai);
    map['vari_veetham'] = Variable<double>(variVeetham);
    map['alavu_vagai'] = Variable<String>(alavuVagai);
    map['alagu'] = Variable<String>(alagu);
    map['created_at'] = Variable<DateTime>(createdAt);
    map['updated_at'] = Variable<DateTime>(updatedAt);
    map['is_deleted'] = Variable<bool>(isDeleted);
    if (!nullToAbsent || deletedAt != null) {
      map['deleted_at'] = Variable<DateTime>(deletedAt);
    }
    return map;
  }

  KooliPorulTableCompanion toCompanion(bool nullToAbsent) {
    return KooliPorulTableCompanion(
      id: Value(id),
      porulPeyar: Value(porulPeyar),
      hsnCode: Value(hsnCode),
      vilai: Value(vilai),
      variVeetham: Value(variVeetham),
      alavuVagai: Value(alavuVagai),
      alagu: Value(alagu),
      createdAt: Value(createdAt),
      updatedAt: Value(updatedAt),
      isDeleted: Value(isDeleted),
      deletedAt: deletedAt == null && nullToAbsent
          ? const Value.absent()
          : Value(deletedAt),
    );
  }

  factory KooliPorulEntry.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return KooliPorulEntry(
      id: serializer.fromJson<int>(json['id']),
      porulPeyar: serializer.fromJson<Map<String, String>>(json['porulPeyar']),
      hsnCode: serializer.fromJson<String>(json['hsnCode']),
      vilai: serializer.fromJson<double>(json['vilai']),
      variVeetham: serializer.fromJson<double>(json['variVeetham']),
      alavuVagai: serializer.fromJson<String>(json['alavuVagai']),
      alagu: serializer.fromJson<String>(json['alagu']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
      isDeleted: serializer.fromJson<bool>(json['isDeleted']),
      deletedAt: serializer.fromJson<DateTime?>(json['deletedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'porulPeyar': serializer.toJson<Map<String, String>>(porulPeyar),
      'hsnCode': serializer.toJson<String>(hsnCode),
      'vilai': serializer.toJson<double>(vilai),
      'variVeetham': serializer.toJson<double>(variVeetham),
      'alavuVagai': serializer.toJson<String>(alavuVagai),
      'alagu': serializer.toJson<String>(alagu),
      'createdAt': serializer.toJson<DateTime>(createdAt),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
      'isDeleted': serializer.toJson<bool>(isDeleted),
      'deletedAt': serializer.toJson<DateTime?>(deletedAt),
    };
  }

  KooliPorulEntry copyWith(
          {int? id,
          Map<String, String>? porulPeyar,
          String? hsnCode,
          double? vilai,
          double? variVeetham,
          String? alavuVagai,
          String? alagu,
          DateTime? createdAt,
          DateTime? updatedAt,
          bool? isDeleted,
          Value<DateTime?> deletedAt = const Value.absent()}) =>
      KooliPorulEntry(
        id: id ?? this.id,
        porulPeyar: porulPeyar ?? this.porulPeyar,
        hsnCode: hsnCode ?? this.hsnCode,
        vilai: vilai ?? this.vilai,
        variVeetham: variVeetham ?? this.variVeetham,
        alavuVagai: alavuVagai ?? this.alavuVagai,
        alagu: alagu ?? this.alagu,
        createdAt: createdAt ?? this.createdAt,
        updatedAt: updatedAt ?? this.updatedAt,
        isDeleted: isDeleted ?? this.isDeleted,
        deletedAt: deletedAt.present ? deletedAt.value : this.deletedAt,
      );
  KooliPorulEntry copyWithCompanion(KooliPorulTableCompanion data) {
    return KooliPorulEntry(
      id: data.id.present ? data.id.value : this.id,
      porulPeyar:
          data.porulPeyar.present ? data.porulPeyar.value : this.porulPeyar,
      hsnCode: data.hsnCode.present ? data.hsnCode.value : this.hsnCode,
      vilai: data.vilai.present ? data.vilai.value : this.vilai,
      variVeetham:
          data.variVeetham.present ? data.variVeetham.value : this.variVeetham,
      alavuVagai:
          data.alavuVagai.present ? data.alavuVagai.value : this.alavuVagai,
      alagu: data.alagu.present ? data.alagu.value : this.alagu,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
      isDeleted: data.isDeleted.present ? data.isDeleted.value : this.isDeleted,
      deletedAt: data.deletedAt.present ? data.deletedAt.value : this.deletedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('KooliPorulEntry(')
          ..write('id: $id, ')
          ..write('porulPeyar: $porulPeyar, ')
          ..write('hsnCode: $hsnCode, ')
          ..write('vilai: $vilai, ')
          ..write('variVeetham: $variVeetham, ')
          ..write('alavuVagai: $alavuVagai, ')
          ..write('alagu: $alagu, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, porulPeyar, hsnCode, vilai, variVeetham,
      alavuVagai, alagu, createdAt, updatedAt, isDeleted, deletedAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is KooliPorulEntry &&
          other.id == this.id &&
          other.porulPeyar == this.porulPeyar &&
          other.hsnCode == this.hsnCode &&
          other.vilai == this.vilai &&
          other.variVeetham == this.variVeetham &&
          other.alavuVagai == this.alavuVagai &&
          other.alagu == this.alagu &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt &&
          other.isDeleted == this.isDeleted &&
          other.deletedAt == this.deletedAt);
}

class KooliPorulTableCompanion extends UpdateCompanion<KooliPorulEntry> {
  final Value<int> id;
  final Value<Map<String, String>> porulPeyar;
  final Value<String> hsnCode;
  final Value<double> vilai;
  final Value<double> variVeetham;
  final Value<String> alavuVagai;
  final Value<String> alagu;
  final Value<DateTime> createdAt;
  final Value<DateTime> updatedAt;
  final Value<bool> isDeleted;
  final Value<DateTime?> deletedAt;
  const KooliPorulTableCompanion({
    this.id = const Value.absent(),
    this.porulPeyar = const Value.absent(),
    this.hsnCode = const Value.absent(),
    this.vilai = const Value.absent(),
    this.variVeetham = const Value.absent(),
    this.alavuVagai = const Value.absent(),
    this.alagu = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  });
  KooliPorulTableCompanion.insert({
    this.id = const Value.absent(),
    this.porulPeyar = const Value.absent(),
    this.hsnCode = const Value.absent(),
    this.vilai = const Value.absent(),
    this.variVeetham = const Value.absent(),
    this.alavuVagai = const Value.absent(),
    this.alagu = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  });
  static Insertable<KooliPorulEntry> custom({
    Expression<int>? id,
    Expression<String>? porulPeyar,
    Expression<String>? hsnCode,
    Expression<double>? vilai,
    Expression<double>? variVeetham,
    Expression<String>? alavuVagai,
    Expression<String>? alagu,
    Expression<DateTime>? createdAt,
    Expression<DateTime>? updatedAt,
    Expression<bool>? isDeleted,
    Expression<DateTime>? deletedAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (porulPeyar != null) 'porul_peyar': porulPeyar,
      if (hsnCode != null) 'hsn_code': hsnCode,
      if (vilai != null) 'vilai': vilai,
      if (variVeetham != null) 'vari_veetham': variVeetham,
      if (alavuVagai != null) 'alavu_vagai': alavuVagai,
      if (alagu != null) 'alagu': alagu,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (isDeleted != null) 'is_deleted': isDeleted,
      if (deletedAt != null) 'deleted_at': deletedAt,
    });
  }

  KooliPorulTableCompanion copyWith(
      {Value<int>? id,
      Value<Map<String, String>>? porulPeyar,
      Value<String>? hsnCode,
      Value<double>? vilai,
      Value<double>? variVeetham,
      Value<String>? alavuVagai,
      Value<String>? alagu,
      Value<DateTime>? createdAt,
      Value<DateTime>? updatedAt,
      Value<bool>? isDeleted,
      Value<DateTime?>? deletedAt}) {
    return KooliPorulTableCompanion(
      id: id ?? this.id,
      porulPeyar: porulPeyar ?? this.porulPeyar,
      hsnCode: hsnCode ?? this.hsnCode,
      vilai: vilai ?? this.vilai,
      variVeetham: variVeetham ?? this.variVeetham,
      alavuVagai: alavuVagai ?? this.alavuVagai,
      alagu: alagu ?? this.alagu,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      isDeleted: isDeleted ?? this.isDeleted,
      deletedAt: deletedAt ?? this.deletedAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (porulPeyar.present) {
      map['porul_peyar'] = Variable<String>(
          $KooliPorulTableTable.$converterporulPeyar.toSql(porulPeyar.value));
    }
    if (hsnCode.present) {
      map['hsn_code'] = Variable<String>(hsnCode.value);
    }
    if (vilai.present) {
      map['vilai'] = Variable<double>(vilai.value);
    }
    if (variVeetham.present) {
      map['vari_veetham'] = Variable<double>(variVeetham.value);
    }
    if (alavuVagai.present) {
      map['alavu_vagai'] = Variable<String>(alavuVagai.value);
    }
    if (alagu.present) {
      map['alagu'] = Variable<String>(alagu.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    if (isDeleted.present) {
      map['is_deleted'] = Variable<bool>(isDeleted.value);
    }
    if (deletedAt.present) {
      map['deleted_at'] = Variable<DateTime>(deletedAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('KooliPorulTableCompanion(')
          ..write('id: $id, ')
          ..write('porulPeyar: $porulPeyar, ')
          ..write('hsnCode: $hsnCode, ')
          ..write('vilai: $vilai, ')
          ..write('variVeetham: $variVeetham, ')
          ..write('alavuVagai: $alavuVagai, ')
          ..write('alagu: $alagu, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }
}

class $KooliPattiyalTableTable extends KooliPattiyalTable
    with TableInfo<$KooliPattiyalTableTable, KooliPattiyalEntry> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $KooliPattiyalTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _niruvanamIdMeta =
      const VerificationMeta('niruvanamId');
  @override
  late final GeneratedColumn<int> niruvanamId = GeneratedColumn<int>(
      'niruvanam_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _patrucheettuEnMeta =
      const VerificationMeta('patrucheettuEn');
  @override
  late final GeneratedColumn<String> patrucheettuEn = GeneratedColumn<String>(
      'patrucheettu_en', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: true,
      defaultConstraints: GeneratedColumn.constraintIsAlways('UNIQUE'));
  static const VerificationMeta _finYearMeta =
      const VerificationMeta('finYear');
  @override
  late final GeneratedColumn<int> finYear = GeneratedColumn<int>(
      'fin_year', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _vanakkamMeta =
      const VerificationMeta('vanakkam');
  @override
  late final GeneratedColumn<int> vanakkam = GeneratedColumn<int>(
      'vanakkam', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(1));
  static const VerificationMeta _pattiyalVagaiMeta =
      const VerificationMeta('pattiyalVagai');
  @override
  late final GeneratedColumn<String> pattiyalVagai = GeneratedColumn<String>(
      'pattiyal_vagai', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('tax-invoice'));
  static const VerificationMeta _vaangunarIdMeta =
      const VerificationMeta('vaangunarId');
  @override
  late final GeneratedColumn<int> vaangunarId = GeneratedColumn<int>(
      'vaangunar_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      vaangunarPeyar = GeneratedColumn<String>(
              'vaangunar_peyar', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliPattiyalTableTable.$convertervaangunarPeyar);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      vaangunarMunvari = GeneratedColumn<String>(
              'vaangunar_munvari', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliPattiyalTableTable.$convertervaangunarMunvari);
  static const VerificationMeta _pattiyalNaalMeta =
      const VerificationMeta('pattiyalNaal');
  @override
  late final GeneratedColumn<DateTime> pattiyalNaal = GeneratedColumn<DateTime>(
      'pattiyal_naal', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _tharavugalMeta =
      const VerificationMeta('tharavugal');
  @override
  late final GeneratedColumn<String> tharavugal = GeneratedColumn<String>(
      'tharavugal', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('[]'));
  static const VerificationMeta _mothaThogaiMeta =
      const VerificationMeta('mothaThogai');
  @override
  late final GeneratedColumn<double> mothaThogai = GeneratedColumn<double>(
      'motha_thogai', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _thallupadiMeta =
      const VerificationMeta('thallupadi');
  @override
  late final GeneratedColumn<double> thallupadi = GeneratedColumn<double>(
      'thallupadi', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _podhuThallupadiMathippuMeta =
      const VerificationMeta('podhuThallupadiMathippu');
  @override
  late final GeneratedColumn<double> podhuThallupadiMathippu =
      GeneratedColumn<double>('podhu_thallupadi_mathippu', aliasedName, false,
          type: DriftSqlType.double,
          requiredDuringInsert: false,
          defaultValue: const Constant(0.0));
  static const VerificationMeta _podhuThallupadiVagaiMeta =
      const VerificationMeta('podhuThallupadiVagai');
  @override
  late final GeneratedColumn<String> podhuThallupadiVagai =
      GeneratedColumn<String>('podhu_thallupadi_vagai', aliasedName, false,
          type: DriftSqlType.string,
          requiredDuringInsert: false,
          defaultValue: const Constant('%'));
  static const VerificationMeta _podhuThallupadiThogaiMeta =
      const VerificationMeta('podhuThallupadiThogai');
  @override
  late final GeneratedColumn<double> podhuThallupadiThogai =
      GeneratedColumn<double>('podhu_thallupadi_thogai', aliasedName, false,
          type: DriftSqlType.double,
          requiredDuringInsert: false,
          defaultValue: const Constant(0.0));
  static const VerificationMeta _variThogaiMeta =
      const VerificationMeta('variThogai');
  @override
  late final GeneratedColumn<double> variThogai = GeneratedColumn<double>(
      'vari_thogai', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _variTharavugalMeta =
      const VerificationMeta('variTharavugal');
  @override
  late final GeneratedColumn<String> variTharavugal = GeneratedColumn<String>(
      'vari_tharavugal', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('{}'));
  static const VerificationMeta _mothaEdaiMeta =
      const VerificationMeta('mothaEdai');
  @override
  late final GeneratedColumn<double> mothaEdai = GeneratedColumn<double>(
      'motha_edai', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _setharamGramsMeta =
      const VerificationMeta('setharamGrams');
  @override
  late final GeneratedColumn<double> setharamGrams = GeneratedColumn<double>(
      'setharam_grams', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _thabaalThogaiMeta =
      const VerificationMeta('thabaalThogai');
  @override
  late final GeneratedColumn<double> thabaalThogai = GeneratedColumn<double>(
      'thabaal_thogai', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _ahimsaPattuThogaiMeta =
      const VerificationMeta('ahimsaPattuThogai');
  @override
  late final GeneratedColumn<double> ahimsaPattuThogai =
      GeneratedColumn<double>('ahimsa_pattu_thogai', aliasedName, false,
          type: DriftSqlType.double,
          requiredDuringInsert: false,
          defaultValue: const Constant(0.0));
  static const VerificationMeta _piravariVugalMeta =
      const VerificationMeta('piravariVugal');
  @override
  late final GeneratedColumn<String> piravariVugal = GeneratedColumn<String>(
      'piravari_vugal', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('[]'));
  static const VerificationMeta _sonthaViruppangalMeta =
      const VerificationMeta('sonthaViruppangal');
  @override
  late final GeneratedColumn<String> sonthaViruppangal =
      GeneratedColumn<String>('sontha_viruppangal', aliasedName, false,
          type: DriftSqlType.string,
          requiredDuringInsert: false,
          defaultValue: const Constant('{}'));
  static const VerificationMeta _nibandhanaigalMeta =
      const VerificationMeta('nibandhanaigal');
  @override
  late final GeneratedColumn<String> nibandhanaigal = GeneratedColumn<String>(
      'nibandhanaigal', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _ullkurippuMeta =
      const VerificationMeta('ullkurippu');
  @override
  late final GeneratedColumn<String> ullkurippu = GeneratedColumn<String>(
      'ullkurippu', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _vangiTharavugalMeta =
      const VerificationMeta('vangiTharavugal');
  @override
  late final GeneratedColumn<String> vangiTharavugal = GeneratedColumn<String>(
      'vangi_tharavugal', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('{}'));
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _updatedAtMeta =
      const VerificationMeta('updatedAt');
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
      'updated_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _isDeletedMeta =
      const VerificationMeta('isDeleted');
  @override
  late final GeneratedColumn<bool> isDeleted = GeneratedColumn<bool>(
      'is_deleted', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_deleted" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _deletedAtMeta =
      const VerificationMeta('deletedAt');
  @override
  late final GeneratedColumn<DateTime> deletedAt = GeneratedColumn<DateTime>(
      'deleted_at', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        niruvanamId,
        patrucheettuEn,
        finYear,
        vanakkam,
        pattiyalVagai,
        vaangunarId,
        vaangunarPeyar,
        vaangunarMunvari,
        pattiyalNaal,
        tharavugal,
        mothaThogai,
        thallupadi,
        podhuThallupadiMathippu,
        podhuThallupadiVagai,
        podhuThallupadiThogai,
        variThogai,
        variTharavugal,
        mothaEdai,
        setharamGrams,
        thabaalThogai,
        ahimsaPattuThogai,
        piravariVugal,
        sonthaViruppangal,
        nibandhanaigal,
        ullkurippu,
        vangiTharavugal,
        createdAt,
        updatedAt,
        isDeleted,
        deletedAt
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'kooli_pattiyal_table';
  @override
  VerificationContext validateIntegrity(Insertable<KooliPattiyalEntry> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('niruvanam_id')) {
      context.handle(
          _niruvanamIdMeta,
          niruvanamId.isAcceptableOrUnknown(
              data['niruvanam_id']!, _niruvanamIdMeta));
    }
    if (data.containsKey('patrucheettu_en')) {
      context.handle(
          _patrucheettuEnMeta,
          patrucheettuEn.isAcceptableOrUnknown(
              data['patrucheettu_en']!, _patrucheettuEnMeta));
    } else if (isInserting) {
      context.missing(_patrucheettuEnMeta);
    }
    if (data.containsKey('fin_year')) {
      context.handle(_finYearMeta,
          finYear.isAcceptableOrUnknown(data['fin_year']!, _finYearMeta));
    } else if (isInserting) {
      context.missing(_finYearMeta);
    }
    if (data.containsKey('vanakkam')) {
      context.handle(_vanakkamMeta,
          vanakkam.isAcceptableOrUnknown(data['vanakkam']!, _vanakkamMeta));
    }
    if (data.containsKey('pattiyal_vagai')) {
      context.handle(
          _pattiyalVagaiMeta,
          pattiyalVagai.isAcceptableOrUnknown(
              data['pattiyal_vagai']!, _pattiyalVagaiMeta));
    }
    if (data.containsKey('vaangunar_id')) {
      context.handle(
          _vaangunarIdMeta,
          vaangunarId.isAcceptableOrUnknown(
              data['vaangunar_id']!, _vaangunarIdMeta));
    }
    if (data.containsKey('pattiyal_naal')) {
      context.handle(
          _pattiyalNaalMeta,
          pattiyalNaal.isAcceptableOrUnknown(
              data['pattiyal_naal']!, _pattiyalNaalMeta));
    }
    if (data.containsKey('tharavugal')) {
      context.handle(
          _tharavugalMeta,
          tharavugal.isAcceptableOrUnknown(
              data['tharavugal']!, _tharavugalMeta));
    }
    if (data.containsKey('motha_thogai')) {
      context.handle(
          _mothaThogaiMeta,
          mothaThogai.isAcceptableOrUnknown(
              data['motha_thogai']!, _mothaThogaiMeta));
    }
    if (data.containsKey('thallupadi')) {
      context.handle(
          _thallupadiMeta,
          thallupadi.isAcceptableOrUnknown(
              data['thallupadi']!, _thallupadiMeta));
    }
    if (data.containsKey('podhu_thallupadi_mathippu')) {
      context.handle(
          _podhuThallupadiMathippuMeta,
          podhuThallupadiMathippu.isAcceptableOrUnknown(
              data['podhu_thallupadi_mathippu']!,
              _podhuThallupadiMathippuMeta));
    }
    if (data.containsKey('podhu_thallupadi_vagai')) {
      context.handle(
          _podhuThallupadiVagaiMeta,
          podhuThallupadiVagai.isAcceptableOrUnknown(
              data['podhu_thallupadi_vagai']!, _podhuThallupadiVagaiMeta));
    }
    if (data.containsKey('podhu_thallupadi_thogai')) {
      context.handle(
          _podhuThallupadiThogaiMeta,
          podhuThallupadiThogai.isAcceptableOrUnknown(
              data['podhu_thallupadi_thogai']!, _podhuThallupadiThogaiMeta));
    }
    if (data.containsKey('vari_thogai')) {
      context.handle(
          _variThogaiMeta,
          variThogai.isAcceptableOrUnknown(
              data['vari_thogai']!, _variThogaiMeta));
    }
    if (data.containsKey('vari_tharavugal')) {
      context.handle(
          _variTharavugalMeta,
          variTharavugal.isAcceptableOrUnknown(
              data['vari_tharavugal']!, _variTharavugalMeta));
    }
    if (data.containsKey('motha_edai')) {
      context.handle(_mothaEdaiMeta,
          mothaEdai.isAcceptableOrUnknown(data['motha_edai']!, _mothaEdaiMeta));
    }
    if (data.containsKey('setharam_grams')) {
      context.handle(
          _setharamGramsMeta,
          setharamGrams.isAcceptableOrUnknown(
              data['setharam_grams']!, _setharamGramsMeta));
    }
    if (data.containsKey('thabaal_thogai')) {
      context.handle(
          _thabaalThogaiMeta,
          thabaalThogai.isAcceptableOrUnknown(
              data['thabaal_thogai']!, _thabaalThogaiMeta));
    }
    if (data.containsKey('ahimsa_pattu_thogai')) {
      context.handle(
          _ahimsaPattuThogaiMeta,
          ahimsaPattuThogai.isAcceptableOrUnknown(
              data['ahimsa_pattu_thogai']!, _ahimsaPattuThogaiMeta));
    }
    if (data.containsKey('piravari_vugal')) {
      context.handle(
          _piravariVugalMeta,
          piravariVugal.isAcceptableOrUnknown(
              data['piravari_vugal']!, _piravariVugalMeta));
    }
    if (data.containsKey('sontha_viruppangal')) {
      context.handle(
          _sonthaViruppangalMeta,
          sonthaViruppangal.isAcceptableOrUnknown(
              data['sontha_viruppangal']!, _sonthaViruppangalMeta));
    }
    if (data.containsKey('nibandhanaigal')) {
      context.handle(
          _nibandhanaigalMeta,
          nibandhanaigal.isAcceptableOrUnknown(
              data['nibandhanaigal']!, _nibandhanaigalMeta));
    }
    if (data.containsKey('ullkurippu')) {
      context.handle(
          _ullkurippuMeta,
          ullkurippu.isAcceptableOrUnknown(
              data['ullkurippu']!, _ullkurippuMeta));
    }
    if (data.containsKey('vangi_tharavugal')) {
      context.handle(
          _vangiTharavugalMeta,
          vangiTharavugal.isAcceptableOrUnknown(
              data['vangi_tharavugal']!, _vangiTharavugalMeta));
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    if (data.containsKey('updated_at')) {
      context.handle(_updatedAtMeta,
          updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta));
    }
    if (data.containsKey('is_deleted')) {
      context.handle(_isDeletedMeta,
          isDeleted.isAcceptableOrUnknown(data['is_deleted']!, _isDeletedMeta));
    }
    if (data.containsKey('deleted_at')) {
      context.handle(_deletedAtMeta,
          deletedAt.isAcceptableOrUnknown(data['deleted_at']!, _deletedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  List<Set<GeneratedColumn>> get uniqueKeys => [
        {niruvanamId, finYear, vanakkam},
      ];
  @override
  KooliPattiyalEntry map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return KooliPattiyalEntry(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      niruvanamId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}niruvanam_id']),
      patrucheettuEn: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}patrucheettu_en'])!,
      finYear: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}fin_year'])!,
      vanakkam: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}vanakkam'])!,
      pattiyalVagai: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}pattiyal_vagai'])!,
      vaangunarId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}vaangunar_id']),
      vaangunarPeyar: $KooliPattiyalTableTable.$convertervaangunarPeyar.fromSql(
          attachedDatabase.typeMapping.read(
              DriftSqlType.string, data['${effectivePrefix}vaangunar_peyar'])!),
      vaangunarMunvari: $KooliPattiyalTableTable.$convertervaangunarMunvari
          .fromSql(attachedDatabase.typeMapping.read(DriftSqlType.string,
              data['${effectivePrefix}vaangunar_munvari'])!),
      pattiyalNaal: attachedDatabase.typeMapping.read(
          DriftSqlType.dateTime, data['${effectivePrefix}pattiyal_naal'])!,
      tharavugal: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}tharavugal'])!,
      mothaThogai: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}motha_thogai'])!,
      thallupadi: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}thallupadi'])!,
      podhuThallupadiMathippu: attachedDatabase.typeMapping.read(
          DriftSqlType.double,
          data['${effectivePrefix}podhu_thallupadi_mathippu'])!,
      podhuThallupadiVagai: attachedDatabase.typeMapping.read(
          DriftSqlType.string,
          data['${effectivePrefix}podhu_thallupadi_vagai'])!,
      podhuThallupadiThogai: attachedDatabase.typeMapping.read(
          DriftSqlType.double,
          data['${effectivePrefix}podhu_thallupadi_thogai'])!,
      variThogai: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}vari_thogai'])!,
      variTharavugal: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}vari_tharavugal'])!,
      mothaEdai: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}motha_edai'])!,
      setharamGrams: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}setharam_grams'])!,
      thabaalThogai: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}thabaal_thogai'])!,
      ahimsaPattuThogai: attachedDatabase.typeMapping.read(
          DriftSqlType.double, data['${effectivePrefix}ahimsa_pattu_thogai'])!,
      piravariVugal: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}piravari_vugal'])!,
      sonthaViruppangal: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}sontha_viruppangal'])!,
      nibandhanaigal: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}nibandhanaigal'])!,
      ullkurippu: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}ullkurippu'])!,
      vangiTharavugal: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}vangi_tharavugal'])!,
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
      updatedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated_at'])!,
      isDeleted: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_deleted'])!,
      deletedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}deleted_at']),
    );
  }

  @override
  $KooliPattiyalTableTable createAlias(String alias) {
    return $KooliPattiyalTableTable(attachedDatabase, alias);
  }

  static TypeConverter<Map<String, String>, String> $convertervaangunarPeyar =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertervaangunarMunvari =
      const MozhiMapConverter();
}

class KooliPattiyalEntry extends DataClass
    implements Insertable<KooliPattiyalEntry> {
  final int id;
  final int? niruvanamId;
  final String patrucheettuEn;
  final int finYear;
  final int vanakkam;
  final String pattiyalVagai;
  final int? vaangunarId;
  final Map<String, String> vaangunarPeyar;
  final Map<String, String> vaangunarMunvari;
  final DateTime pattiyalNaal;
  final String tharavugal;
  final double mothaThogai;
  final double thallupadi;
  final double podhuThallupadiMathippu;
  final String podhuThallupadiVagai;
  final double podhuThallupadiThogai;
  final double variThogai;
  final String variTharavugal;
  final double mothaEdai;
  final double setharamGrams;
  final double thabaalThogai;
  final double ahimsaPattuThogai;
  final String piravariVugal;
  final String sonthaViruppangal;
  final String nibandhanaigal;
  final String ullkurippu;
  final String vangiTharavugal;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isDeleted;
  final DateTime? deletedAt;
  const KooliPattiyalEntry(
      {required this.id,
      this.niruvanamId,
      required this.patrucheettuEn,
      required this.finYear,
      required this.vanakkam,
      required this.pattiyalVagai,
      this.vaangunarId,
      required this.vaangunarPeyar,
      required this.vaangunarMunvari,
      required this.pattiyalNaal,
      required this.tharavugal,
      required this.mothaThogai,
      required this.thallupadi,
      required this.podhuThallupadiMathippu,
      required this.podhuThallupadiVagai,
      required this.podhuThallupadiThogai,
      required this.variThogai,
      required this.variTharavugal,
      required this.mothaEdai,
      required this.setharamGrams,
      required this.thabaalThogai,
      required this.ahimsaPattuThogai,
      required this.piravariVugal,
      required this.sonthaViruppangal,
      required this.nibandhanaigal,
      required this.ullkurippu,
      required this.vangiTharavugal,
      required this.createdAt,
      required this.updatedAt,
      required this.isDeleted,
      this.deletedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    if (!nullToAbsent || niruvanamId != null) {
      map['niruvanam_id'] = Variable<int>(niruvanamId);
    }
    map['patrucheettu_en'] = Variable<String>(patrucheettuEn);
    map['fin_year'] = Variable<int>(finYear);
    map['vanakkam'] = Variable<int>(vanakkam);
    map['pattiyal_vagai'] = Variable<String>(pattiyalVagai);
    if (!nullToAbsent || vaangunarId != null) {
      map['vaangunar_id'] = Variable<int>(vaangunarId);
    }
    {
      map['vaangunar_peyar'] = Variable<String>($KooliPattiyalTableTable
          .$convertervaangunarPeyar
          .toSql(vaangunarPeyar));
    }
    {
      map['vaangunar_munvari'] = Variable<String>($KooliPattiyalTableTable
          .$convertervaangunarMunvari
          .toSql(vaangunarMunvari));
    }
    map['pattiyal_naal'] = Variable<DateTime>(pattiyalNaal);
    map['tharavugal'] = Variable<String>(tharavugal);
    map['motha_thogai'] = Variable<double>(mothaThogai);
    map['thallupadi'] = Variable<double>(thallupadi);
    map['podhu_thallupadi_mathippu'] =
        Variable<double>(podhuThallupadiMathippu);
    map['podhu_thallupadi_vagai'] = Variable<String>(podhuThallupadiVagai);
    map['podhu_thallupadi_thogai'] = Variable<double>(podhuThallupadiThogai);
    map['vari_thogai'] = Variable<double>(variThogai);
    map['vari_tharavugal'] = Variable<String>(variTharavugal);
    map['motha_edai'] = Variable<double>(mothaEdai);
    map['setharam_grams'] = Variable<double>(setharamGrams);
    map['thabaal_thogai'] = Variable<double>(thabaalThogai);
    map['ahimsa_pattu_thogai'] = Variable<double>(ahimsaPattuThogai);
    map['piravari_vugal'] = Variable<String>(piravariVugal);
    map['sontha_viruppangal'] = Variable<String>(sonthaViruppangal);
    map['nibandhanaigal'] = Variable<String>(nibandhanaigal);
    map['ullkurippu'] = Variable<String>(ullkurippu);
    map['vangi_tharavugal'] = Variable<String>(vangiTharavugal);
    map['created_at'] = Variable<DateTime>(createdAt);
    map['updated_at'] = Variable<DateTime>(updatedAt);
    map['is_deleted'] = Variable<bool>(isDeleted);
    if (!nullToAbsent || deletedAt != null) {
      map['deleted_at'] = Variable<DateTime>(deletedAt);
    }
    return map;
  }

  KooliPattiyalTableCompanion toCompanion(bool nullToAbsent) {
    return KooliPattiyalTableCompanion(
      id: Value(id),
      niruvanamId: niruvanamId == null && nullToAbsent
          ? const Value.absent()
          : Value(niruvanamId),
      patrucheettuEn: Value(patrucheettuEn),
      finYear: Value(finYear),
      vanakkam: Value(vanakkam),
      pattiyalVagai: Value(pattiyalVagai),
      vaangunarId: vaangunarId == null && nullToAbsent
          ? const Value.absent()
          : Value(vaangunarId),
      vaangunarPeyar: Value(vaangunarPeyar),
      vaangunarMunvari: Value(vaangunarMunvari),
      pattiyalNaal: Value(pattiyalNaal),
      tharavugal: Value(tharavugal),
      mothaThogai: Value(mothaThogai),
      thallupadi: Value(thallupadi),
      podhuThallupadiMathippu: Value(podhuThallupadiMathippu),
      podhuThallupadiVagai: Value(podhuThallupadiVagai),
      podhuThallupadiThogai: Value(podhuThallupadiThogai),
      variThogai: Value(variThogai),
      variTharavugal: Value(variTharavugal),
      mothaEdai: Value(mothaEdai),
      setharamGrams: Value(setharamGrams),
      thabaalThogai: Value(thabaalThogai),
      ahimsaPattuThogai: Value(ahimsaPattuThogai),
      piravariVugal: Value(piravariVugal),
      sonthaViruppangal: Value(sonthaViruppangal),
      nibandhanaigal: Value(nibandhanaigal),
      ullkurippu: Value(ullkurippu),
      vangiTharavugal: Value(vangiTharavugal),
      createdAt: Value(createdAt),
      updatedAt: Value(updatedAt),
      isDeleted: Value(isDeleted),
      deletedAt: deletedAt == null && nullToAbsent
          ? const Value.absent()
          : Value(deletedAt),
    );
  }

  factory KooliPattiyalEntry.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return KooliPattiyalEntry(
      id: serializer.fromJson<int>(json['id']),
      niruvanamId: serializer.fromJson<int?>(json['niruvanamId']),
      patrucheettuEn: serializer.fromJson<String>(json['patrucheettuEn']),
      finYear: serializer.fromJson<int>(json['finYear']),
      vanakkam: serializer.fromJson<int>(json['vanakkam']),
      pattiyalVagai: serializer.fromJson<String>(json['pattiyalVagai']),
      vaangunarId: serializer.fromJson<int?>(json['vaangunarId']),
      vaangunarPeyar:
          serializer.fromJson<Map<String, String>>(json['vaangunarPeyar']),
      vaangunarMunvari:
          serializer.fromJson<Map<String, String>>(json['vaangunarMunvari']),
      pattiyalNaal: serializer.fromJson<DateTime>(json['pattiyalNaal']),
      tharavugal: serializer.fromJson<String>(json['tharavugal']),
      mothaThogai: serializer.fromJson<double>(json['mothaThogai']),
      thallupadi: serializer.fromJson<double>(json['thallupadi']),
      podhuThallupadiMathippu:
          serializer.fromJson<double>(json['podhuThallupadiMathippu']),
      podhuThallupadiVagai:
          serializer.fromJson<String>(json['podhuThallupadiVagai']),
      podhuThallupadiThogai:
          serializer.fromJson<double>(json['podhuThallupadiThogai']),
      variThogai: serializer.fromJson<double>(json['variThogai']),
      variTharavugal: serializer.fromJson<String>(json['variTharavugal']),
      mothaEdai: serializer.fromJson<double>(json['mothaEdai']),
      setharamGrams: serializer.fromJson<double>(json['setharamGrams']),
      thabaalThogai: serializer.fromJson<double>(json['thabaalThogai']),
      ahimsaPattuThogai: serializer.fromJson<double>(json['ahimsaPattuThogai']),
      piravariVugal: serializer.fromJson<String>(json['piravariVugal']),
      sonthaViruppangal: serializer.fromJson<String>(json['sonthaViruppangal']),
      nibandhanaigal: serializer.fromJson<String>(json['nibandhanaigal']),
      ullkurippu: serializer.fromJson<String>(json['ullkurippu']),
      vangiTharavugal: serializer.fromJson<String>(json['vangiTharavugal']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
      isDeleted: serializer.fromJson<bool>(json['isDeleted']),
      deletedAt: serializer.fromJson<DateTime?>(json['deletedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'niruvanamId': serializer.toJson<int?>(niruvanamId),
      'patrucheettuEn': serializer.toJson<String>(patrucheettuEn),
      'finYear': serializer.toJson<int>(finYear),
      'vanakkam': serializer.toJson<int>(vanakkam),
      'pattiyalVagai': serializer.toJson<String>(pattiyalVagai),
      'vaangunarId': serializer.toJson<int?>(vaangunarId),
      'vaangunarPeyar': serializer.toJson<Map<String, String>>(vaangunarPeyar),
      'vaangunarMunvari':
          serializer.toJson<Map<String, String>>(vaangunarMunvari),
      'pattiyalNaal': serializer.toJson<DateTime>(pattiyalNaal),
      'tharavugal': serializer.toJson<String>(tharavugal),
      'mothaThogai': serializer.toJson<double>(mothaThogai),
      'thallupadi': serializer.toJson<double>(thallupadi),
      'podhuThallupadiMathippu':
          serializer.toJson<double>(podhuThallupadiMathippu),
      'podhuThallupadiVagai': serializer.toJson<String>(podhuThallupadiVagai),
      'podhuThallupadiThogai': serializer.toJson<double>(podhuThallupadiThogai),
      'variThogai': serializer.toJson<double>(variThogai),
      'variTharavugal': serializer.toJson<String>(variTharavugal),
      'mothaEdai': serializer.toJson<double>(mothaEdai),
      'setharamGrams': serializer.toJson<double>(setharamGrams),
      'thabaalThogai': serializer.toJson<double>(thabaalThogai),
      'ahimsaPattuThogai': serializer.toJson<double>(ahimsaPattuThogai),
      'piravariVugal': serializer.toJson<String>(piravariVugal),
      'sonthaViruppangal': serializer.toJson<String>(sonthaViruppangal),
      'nibandhanaigal': serializer.toJson<String>(nibandhanaigal),
      'ullkurippu': serializer.toJson<String>(ullkurippu),
      'vangiTharavugal': serializer.toJson<String>(vangiTharavugal),
      'createdAt': serializer.toJson<DateTime>(createdAt),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
      'isDeleted': serializer.toJson<bool>(isDeleted),
      'deletedAt': serializer.toJson<DateTime?>(deletedAt),
    };
  }

  KooliPattiyalEntry copyWith(
          {int? id,
          Value<int?> niruvanamId = const Value.absent(),
          String? patrucheettuEn,
          int? finYear,
          int? vanakkam,
          String? pattiyalVagai,
          Value<int?> vaangunarId = const Value.absent(),
          Map<String, String>? vaangunarPeyar,
          Map<String, String>? vaangunarMunvari,
          DateTime? pattiyalNaal,
          String? tharavugal,
          double? mothaThogai,
          double? thallupadi,
          double? podhuThallupadiMathippu,
          String? podhuThallupadiVagai,
          double? podhuThallupadiThogai,
          double? variThogai,
          String? variTharavugal,
          double? mothaEdai,
          double? setharamGrams,
          double? thabaalThogai,
          double? ahimsaPattuThogai,
          String? piravariVugal,
          String? sonthaViruppangal,
          String? nibandhanaigal,
          String? ullkurippu,
          String? vangiTharavugal,
          DateTime? createdAt,
          DateTime? updatedAt,
          bool? isDeleted,
          Value<DateTime?> deletedAt = const Value.absent()}) =>
      KooliPattiyalEntry(
        id: id ?? this.id,
        niruvanamId: niruvanamId.present ? niruvanamId.value : this.niruvanamId,
        patrucheettuEn: patrucheettuEn ?? this.patrucheettuEn,
        finYear: finYear ?? this.finYear,
        vanakkam: vanakkam ?? this.vanakkam,
        pattiyalVagai: pattiyalVagai ?? this.pattiyalVagai,
        vaangunarId: vaangunarId.present ? vaangunarId.value : this.vaangunarId,
        vaangunarPeyar: vaangunarPeyar ?? this.vaangunarPeyar,
        vaangunarMunvari: vaangunarMunvari ?? this.vaangunarMunvari,
        pattiyalNaal: pattiyalNaal ?? this.pattiyalNaal,
        tharavugal: tharavugal ?? this.tharavugal,
        mothaThogai: mothaThogai ?? this.mothaThogai,
        thallupadi: thallupadi ?? this.thallupadi,
        podhuThallupadiMathippu:
            podhuThallupadiMathippu ?? this.podhuThallupadiMathippu,
        podhuThallupadiVagai: podhuThallupadiVagai ?? this.podhuThallupadiVagai,
        podhuThallupadiThogai:
            podhuThallupadiThogai ?? this.podhuThallupadiThogai,
        variThogai: variThogai ?? this.variThogai,
        variTharavugal: variTharavugal ?? this.variTharavugal,
        mothaEdai: mothaEdai ?? this.mothaEdai,
        setharamGrams: setharamGrams ?? this.setharamGrams,
        thabaalThogai: thabaalThogai ?? this.thabaalThogai,
        ahimsaPattuThogai: ahimsaPattuThogai ?? this.ahimsaPattuThogai,
        piravariVugal: piravariVugal ?? this.piravariVugal,
        sonthaViruppangal: sonthaViruppangal ?? this.sonthaViruppangal,
        nibandhanaigal: nibandhanaigal ?? this.nibandhanaigal,
        ullkurippu: ullkurippu ?? this.ullkurippu,
        vangiTharavugal: vangiTharavugal ?? this.vangiTharavugal,
        createdAt: createdAt ?? this.createdAt,
        updatedAt: updatedAt ?? this.updatedAt,
        isDeleted: isDeleted ?? this.isDeleted,
        deletedAt: deletedAt.present ? deletedAt.value : this.deletedAt,
      );
  KooliPattiyalEntry copyWithCompanion(KooliPattiyalTableCompanion data) {
    return KooliPattiyalEntry(
      id: data.id.present ? data.id.value : this.id,
      niruvanamId:
          data.niruvanamId.present ? data.niruvanamId.value : this.niruvanamId,
      patrucheettuEn: data.patrucheettuEn.present
          ? data.patrucheettuEn.value
          : this.patrucheettuEn,
      finYear: data.finYear.present ? data.finYear.value : this.finYear,
      vanakkam: data.vanakkam.present ? data.vanakkam.value : this.vanakkam,
      pattiyalVagai: data.pattiyalVagai.present
          ? data.pattiyalVagai.value
          : this.pattiyalVagai,
      vaangunarId:
          data.vaangunarId.present ? data.vaangunarId.value : this.vaangunarId,
      vaangunarPeyar: data.vaangunarPeyar.present
          ? data.vaangunarPeyar.value
          : this.vaangunarPeyar,
      vaangunarMunvari: data.vaangunarMunvari.present
          ? data.vaangunarMunvari.value
          : this.vaangunarMunvari,
      pattiyalNaal: data.pattiyalNaal.present
          ? data.pattiyalNaal.value
          : this.pattiyalNaal,
      tharavugal:
          data.tharavugal.present ? data.tharavugal.value : this.tharavugal,
      mothaThogai:
          data.mothaThogai.present ? data.mothaThogai.value : this.mothaThogai,
      thallupadi:
          data.thallupadi.present ? data.thallupadi.value : this.thallupadi,
      podhuThallupadiMathippu: data.podhuThallupadiMathippu.present
          ? data.podhuThallupadiMathippu.value
          : this.podhuThallupadiMathippu,
      podhuThallupadiVagai: data.podhuThallupadiVagai.present
          ? data.podhuThallupadiVagai.value
          : this.podhuThallupadiVagai,
      podhuThallupadiThogai: data.podhuThallupadiThogai.present
          ? data.podhuThallupadiThogai.value
          : this.podhuThallupadiThogai,
      variThogai:
          data.variThogai.present ? data.variThogai.value : this.variThogai,
      variTharavugal: data.variTharavugal.present
          ? data.variTharavugal.value
          : this.variTharavugal,
      mothaEdai: data.mothaEdai.present ? data.mothaEdai.value : this.mothaEdai,
      setharamGrams: data.setharamGrams.present
          ? data.setharamGrams.value
          : this.setharamGrams,
      thabaalThogai: data.thabaalThogai.present
          ? data.thabaalThogai.value
          : this.thabaalThogai,
      ahimsaPattuThogai: data.ahimsaPattuThogai.present
          ? data.ahimsaPattuThogai.value
          : this.ahimsaPattuThogai,
      piravariVugal: data.piravariVugal.present
          ? data.piravariVugal.value
          : this.piravariVugal,
      sonthaViruppangal: data.sonthaViruppangal.present
          ? data.sonthaViruppangal.value
          : this.sonthaViruppangal,
      nibandhanaigal: data.nibandhanaigal.present
          ? data.nibandhanaigal.value
          : this.nibandhanaigal,
      ullkurippu:
          data.ullkurippu.present ? data.ullkurippu.value : this.ullkurippu,
      vangiTharavugal: data.vangiTharavugal.present
          ? data.vangiTharavugal.value
          : this.vangiTharavugal,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
      isDeleted: data.isDeleted.present ? data.isDeleted.value : this.isDeleted,
      deletedAt: data.deletedAt.present ? data.deletedAt.value : this.deletedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('KooliPattiyalEntry(')
          ..write('id: $id, ')
          ..write('niruvanamId: $niruvanamId, ')
          ..write('patrucheettuEn: $patrucheettuEn, ')
          ..write('finYear: $finYear, ')
          ..write('vanakkam: $vanakkam, ')
          ..write('pattiyalVagai: $pattiyalVagai, ')
          ..write('vaangunarId: $vaangunarId, ')
          ..write('vaangunarPeyar: $vaangunarPeyar, ')
          ..write('vaangunarMunvari: $vaangunarMunvari, ')
          ..write('pattiyalNaal: $pattiyalNaal, ')
          ..write('tharavugal: $tharavugal, ')
          ..write('mothaThogai: $mothaThogai, ')
          ..write('thallupadi: $thallupadi, ')
          ..write('podhuThallupadiMathippu: $podhuThallupadiMathippu, ')
          ..write('podhuThallupadiVagai: $podhuThallupadiVagai, ')
          ..write('podhuThallupadiThogai: $podhuThallupadiThogai, ')
          ..write('variThogai: $variThogai, ')
          ..write('variTharavugal: $variTharavugal, ')
          ..write('mothaEdai: $mothaEdai, ')
          ..write('setharamGrams: $setharamGrams, ')
          ..write('thabaalThogai: $thabaalThogai, ')
          ..write('ahimsaPattuThogai: $ahimsaPattuThogai, ')
          ..write('piravariVugal: $piravariVugal, ')
          ..write('sonthaViruppangal: $sonthaViruppangal, ')
          ..write('nibandhanaigal: $nibandhanaigal, ')
          ..write('ullkurippu: $ullkurippu, ')
          ..write('vangiTharavugal: $vangiTharavugal, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hashAll([
        id,
        niruvanamId,
        patrucheettuEn,
        finYear,
        vanakkam,
        pattiyalVagai,
        vaangunarId,
        vaangunarPeyar,
        vaangunarMunvari,
        pattiyalNaal,
        tharavugal,
        mothaThogai,
        thallupadi,
        podhuThallupadiMathippu,
        podhuThallupadiVagai,
        podhuThallupadiThogai,
        variThogai,
        variTharavugal,
        mothaEdai,
        setharamGrams,
        thabaalThogai,
        ahimsaPattuThogai,
        piravariVugal,
        sonthaViruppangal,
        nibandhanaigal,
        ullkurippu,
        vangiTharavugal,
        createdAt,
        updatedAt,
        isDeleted,
        deletedAt
      ]);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is KooliPattiyalEntry &&
          other.id == this.id &&
          other.niruvanamId == this.niruvanamId &&
          other.patrucheettuEn == this.patrucheettuEn &&
          other.finYear == this.finYear &&
          other.vanakkam == this.vanakkam &&
          other.pattiyalVagai == this.pattiyalVagai &&
          other.vaangunarId == this.vaangunarId &&
          other.vaangunarPeyar == this.vaangunarPeyar &&
          other.vaangunarMunvari == this.vaangunarMunvari &&
          other.pattiyalNaal == this.pattiyalNaal &&
          other.tharavugal == this.tharavugal &&
          other.mothaThogai == this.mothaThogai &&
          other.thallupadi == this.thallupadi &&
          other.podhuThallupadiMathippu == this.podhuThallupadiMathippu &&
          other.podhuThallupadiVagai == this.podhuThallupadiVagai &&
          other.podhuThallupadiThogai == this.podhuThallupadiThogai &&
          other.variThogai == this.variThogai &&
          other.variTharavugal == this.variTharavugal &&
          other.mothaEdai == this.mothaEdai &&
          other.setharamGrams == this.setharamGrams &&
          other.thabaalThogai == this.thabaalThogai &&
          other.ahimsaPattuThogai == this.ahimsaPattuThogai &&
          other.piravariVugal == this.piravariVugal &&
          other.sonthaViruppangal == this.sonthaViruppangal &&
          other.nibandhanaigal == this.nibandhanaigal &&
          other.ullkurippu == this.ullkurippu &&
          other.vangiTharavugal == this.vangiTharavugal &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt &&
          other.isDeleted == this.isDeleted &&
          other.deletedAt == this.deletedAt);
}

class KooliPattiyalTableCompanion extends UpdateCompanion<KooliPattiyalEntry> {
  final Value<int> id;
  final Value<int?> niruvanamId;
  final Value<String> patrucheettuEn;
  final Value<int> finYear;
  final Value<int> vanakkam;
  final Value<String> pattiyalVagai;
  final Value<int?> vaangunarId;
  final Value<Map<String, String>> vaangunarPeyar;
  final Value<Map<String, String>> vaangunarMunvari;
  final Value<DateTime> pattiyalNaal;
  final Value<String> tharavugal;
  final Value<double> mothaThogai;
  final Value<double> thallupadi;
  final Value<double> podhuThallupadiMathippu;
  final Value<String> podhuThallupadiVagai;
  final Value<double> podhuThallupadiThogai;
  final Value<double> variThogai;
  final Value<String> variTharavugal;
  final Value<double> mothaEdai;
  final Value<double> setharamGrams;
  final Value<double> thabaalThogai;
  final Value<double> ahimsaPattuThogai;
  final Value<String> piravariVugal;
  final Value<String> sonthaViruppangal;
  final Value<String> nibandhanaigal;
  final Value<String> ullkurippu;
  final Value<String> vangiTharavugal;
  final Value<DateTime> createdAt;
  final Value<DateTime> updatedAt;
  final Value<bool> isDeleted;
  final Value<DateTime?> deletedAt;
  const KooliPattiyalTableCompanion({
    this.id = const Value.absent(),
    this.niruvanamId = const Value.absent(),
    this.patrucheettuEn = const Value.absent(),
    this.finYear = const Value.absent(),
    this.vanakkam = const Value.absent(),
    this.pattiyalVagai = const Value.absent(),
    this.vaangunarId = const Value.absent(),
    this.vaangunarPeyar = const Value.absent(),
    this.vaangunarMunvari = const Value.absent(),
    this.pattiyalNaal = const Value.absent(),
    this.tharavugal = const Value.absent(),
    this.mothaThogai = const Value.absent(),
    this.thallupadi = const Value.absent(),
    this.podhuThallupadiMathippu = const Value.absent(),
    this.podhuThallupadiVagai = const Value.absent(),
    this.podhuThallupadiThogai = const Value.absent(),
    this.variThogai = const Value.absent(),
    this.variTharavugal = const Value.absent(),
    this.mothaEdai = const Value.absent(),
    this.setharamGrams = const Value.absent(),
    this.thabaalThogai = const Value.absent(),
    this.ahimsaPattuThogai = const Value.absent(),
    this.piravariVugal = const Value.absent(),
    this.sonthaViruppangal = const Value.absent(),
    this.nibandhanaigal = const Value.absent(),
    this.ullkurippu = const Value.absent(),
    this.vangiTharavugal = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  });
  KooliPattiyalTableCompanion.insert({
    this.id = const Value.absent(),
    this.niruvanamId = const Value.absent(),
    required String patrucheettuEn,
    required int finYear,
    this.vanakkam = const Value.absent(),
    this.pattiyalVagai = const Value.absent(),
    this.vaangunarId = const Value.absent(),
    this.vaangunarPeyar = const Value.absent(),
    this.vaangunarMunvari = const Value.absent(),
    this.pattiyalNaal = const Value.absent(),
    this.tharavugal = const Value.absent(),
    this.mothaThogai = const Value.absent(),
    this.thallupadi = const Value.absent(),
    this.podhuThallupadiMathippu = const Value.absent(),
    this.podhuThallupadiVagai = const Value.absent(),
    this.podhuThallupadiThogai = const Value.absent(),
    this.variThogai = const Value.absent(),
    this.variTharavugal = const Value.absent(),
    this.mothaEdai = const Value.absent(),
    this.setharamGrams = const Value.absent(),
    this.thabaalThogai = const Value.absent(),
    this.ahimsaPattuThogai = const Value.absent(),
    this.piravariVugal = const Value.absent(),
    this.sonthaViruppangal = const Value.absent(),
    this.nibandhanaigal = const Value.absent(),
    this.ullkurippu = const Value.absent(),
    this.vangiTharavugal = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  })  : patrucheettuEn = Value(patrucheettuEn),
        finYear = Value(finYear);
  static Insertable<KooliPattiyalEntry> custom({
    Expression<int>? id,
    Expression<int>? niruvanamId,
    Expression<String>? patrucheettuEn,
    Expression<int>? finYear,
    Expression<int>? vanakkam,
    Expression<String>? pattiyalVagai,
    Expression<int>? vaangunarId,
    Expression<String>? vaangunarPeyar,
    Expression<String>? vaangunarMunvari,
    Expression<DateTime>? pattiyalNaal,
    Expression<String>? tharavugal,
    Expression<double>? mothaThogai,
    Expression<double>? thallupadi,
    Expression<double>? podhuThallupadiMathippu,
    Expression<String>? podhuThallupadiVagai,
    Expression<double>? podhuThallupadiThogai,
    Expression<double>? variThogai,
    Expression<String>? variTharavugal,
    Expression<double>? mothaEdai,
    Expression<double>? setharamGrams,
    Expression<double>? thabaalThogai,
    Expression<double>? ahimsaPattuThogai,
    Expression<String>? piravariVugal,
    Expression<String>? sonthaViruppangal,
    Expression<String>? nibandhanaigal,
    Expression<String>? ullkurippu,
    Expression<String>? vangiTharavugal,
    Expression<DateTime>? createdAt,
    Expression<DateTime>? updatedAt,
    Expression<bool>? isDeleted,
    Expression<DateTime>? deletedAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (niruvanamId != null) 'niruvanam_id': niruvanamId,
      if (patrucheettuEn != null) 'patrucheettu_en': patrucheettuEn,
      if (finYear != null) 'fin_year': finYear,
      if (vanakkam != null) 'vanakkam': vanakkam,
      if (pattiyalVagai != null) 'pattiyal_vagai': pattiyalVagai,
      if (vaangunarId != null) 'vaangunar_id': vaangunarId,
      if (vaangunarPeyar != null) 'vaangunar_peyar': vaangunarPeyar,
      if (vaangunarMunvari != null) 'vaangunar_munvari': vaangunarMunvari,
      if (pattiyalNaal != null) 'pattiyal_naal': pattiyalNaal,
      if (tharavugal != null) 'tharavugal': tharavugal,
      if (mothaThogai != null) 'motha_thogai': mothaThogai,
      if (thallupadi != null) 'thallupadi': thallupadi,
      if (podhuThallupadiMathippu != null)
        'podhu_thallupadi_mathippu': podhuThallupadiMathippu,
      if (podhuThallupadiVagai != null)
        'podhu_thallupadi_vagai': podhuThallupadiVagai,
      if (podhuThallupadiThogai != null)
        'podhu_thallupadi_thogai': podhuThallupadiThogai,
      if (variThogai != null) 'vari_thogai': variThogai,
      if (variTharavugal != null) 'vari_tharavugal': variTharavugal,
      if (mothaEdai != null) 'motha_edai': mothaEdai,
      if (setharamGrams != null) 'setharam_grams': setharamGrams,
      if (thabaalThogai != null) 'thabaal_thogai': thabaalThogai,
      if (ahimsaPattuThogai != null) 'ahimsa_pattu_thogai': ahimsaPattuThogai,
      if (piravariVugal != null) 'piravari_vugal': piravariVugal,
      if (sonthaViruppangal != null) 'sontha_viruppangal': sonthaViruppangal,
      if (nibandhanaigal != null) 'nibandhanaigal': nibandhanaigal,
      if (ullkurippu != null) 'ullkurippu': ullkurippu,
      if (vangiTharavugal != null) 'vangi_tharavugal': vangiTharavugal,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (isDeleted != null) 'is_deleted': isDeleted,
      if (deletedAt != null) 'deleted_at': deletedAt,
    });
  }

  KooliPattiyalTableCompanion copyWith(
      {Value<int>? id,
      Value<int?>? niruvanamId,
      Value<String>? patrucheettuEn,
      Value<int>? finYear,
      Value<int>? vanakkam,
      Value<String>? pattiyalVagai,
      Value<int?>? vaangunarId,
      Value<Map<String, String>>? vaangunarPeyar,
      Value<Map<String, String>>? vaangunarMunvari,
      Value<DateTime>? pattiyalNaal,
      Value<String>? tharavugal,
      Value<double>? mothaThogai,
      Value<double>? thallupadi,
      Value<double>? podhuThallupadiMathippu,
      Value<String>? podhuThallupadiVagai,
      Value<double>? podhuThallupadiThogai,
      Value<double>? variThogai,
      Value<String>? variTharavugal,
      Value<double>? mothaEdai,
      Value<double>? setharamGrams,
      Value<double>? thabaalThogai,
      Value<double>? ahimsaPattuThogai,
      Value<String>? piravariVugal,
      Value<String>? sonthaViruppangal,
      Value<String>? nibandhanaigal,
      Value<String>? ullkurippu,
      Value<String>? vangiTharavugal,
      Value<DateTime>? createdAt,
      Value<DateTime>? updatedAt,
      Value<bool>? isDeleted,
      Value<DateTime?>? deletedAt}) {
    return KooliPattiyalTableCompanion(
      id: id ?? this.id,
      niruvanamId: niruvanamId ?? this.niruvanamId,
      patrucheettuEn: patrucheettuEn ?? this.patrucheettuEn,
      finYear: finYear ?? this.finYear,
      vanakkam: vanakkam ?? this.vanakkam,
      pattiyalVagai: pattiyalVagai ?? this.pattiyalVagai,
      vaangunarId: vaangunarId ?? this.vaangunarId,
      vaangunarPeyar: vaangunarPeyar ?? this.vaangunarPeyar,
      vaangunarMunvari: vaangunarMunvari ?? this.vaangunarMunvari,
      pattiyalNaal: pattiyalNaal ?? this.pattiyalNaal,
      tharavugal: tharavugal ?? this.tharavugal,
      mothaThogai: mothaThogai ?? this.mothaThogai,
      thallupadi: thallupadi ?? this.thallupadi,
      podhuThallupadiMathippu:
          podhuThallupadiMathippu ?? this.podhuThallupadiMathippu,
      podhuThallupadiVagai: podhuThallupadiVagai ?? this.podhuThallupadiVagai,
      podhuThallupadiThogai:
          podhuThallupadiThogai ?? this.podhuThallupadiThogai,
      variThogai: variThogai ?? this.variThogai,
      variTharavugal: variTharavugal ?? this.variTharavugal,
      mothaEdai: mothaEdai ?? this.mothaEdai,
      setharamGrams: setharamGrams ?? this.setharamGrams,
      thabaalThogai: thabaalThogai ?? this.thabaalThogai,
      ahimsaPattuThogai: ahimsaPattuThogai ?? this.ahimsaPattuThogai,
      piravariVugal: piravariVugal ?? this.piravariVugal,
      sonthaViruppangal: sonthaViruppangal ?? this.sonthaViruppangal,
      nibandhanaigal: nibandhanaigal ?? this.nibandhanaigal,
      ullkurippu: ullkurippu ?? this.ullkurippu,
      vangiTharavugal: vangiTharavugal ?? this.vangiTharavugal,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      isDeleted: isDeleted ?? this.isDeleted,
      deletedAt: deletedAt ?? this.deletedAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (niruvanamId.present) {
      map['niruvanam_id'] = Variable<int>(niruvanamId.value);
    }
    if (patrucheettuEn.present) {
      map['patrucheettu_en'] = Variable<String>(patrucheettuEn.value);
    }
    if (finYear.present) {
      map['fin_year'] = Variable<int>(finYear.value);
    }
    if (vanakkam.present) {
      map['vanakkam'] = Variable<int>(vanakkam.value);
    }
    if (pattiyalVagai.present) {
      map['pattiyal_vagai'] = Variable<String>(pattiyalVagai.value);
    }
    if (vaangunarId.present) {
      map['vaangunar_id'] = Variable<int>(vaangunarId.value);
    }
    if (vaangunarPeyar.present) {
      map['vaangunar_peyar'] = Variable<String>($KooliPattiyalTableTable
          .$convertervaangunarPeyar
          .toSql(vaangunarPeyar.value));
    }
    if (vaangunarMunvari.present) {
      map['vaangunar_munvari'] = Variable<String>($KooliPattiyalTableTable
          .$convertervaangunarMunvari
          .toSql(vaangunarMunvari.value));
    }
    if (pattiyalNaal.present) {
      map['pattiyal_naal'] = Variable<DateTime>(pattiyalNaal.value);
    }
    if (tharavugal.present) {
      map['tharavugal'] = Variable<String>(tharavugal.value);
    }
    if (mothaThogai.present) {
      map['motha_thogai'] = Variable<double>(mothaThogai.value);
    }
    if (thallupadi.present) {
      map['thallupadi'] = Variable<double>(thallupadi.value);
    }
    if (podhuThallupadiMathippu.present) {
      map['podhu_thallupadi_mathippu'] =
          Variable<double>(podhuThallupadiMathippu.value);
    }
    if (podhuThallupadiVagai.present) {
      map['podhu_thallupadi_vagai'] =
          Variable<String>(podhuThallupadiVagai.value);
    }
    if (podhuThallupadiThogai.present) {
      map['podhu_thallupadi_thogai'] =
          Variable<double>(podhuThallupadiThogai.value);
    }
    if (variThogai.present) {
      map['vari_thogai'] = Variable<double>(variThogai.value);
    }
    if (variTharavugal.present) {
      map['vari_tharavugal'] = Variable<String>(variTharavugal.value);
    }
    if (mothaEdai.present) {
      map['motha_edai'] = Variable<double>(mothaEdai.value);
    }
    if (setharamGrams.present) {
      map['setharam_grams'] = Variable<double>(setharamGrams.value);
    }
    if (thabaalThogai.present) {
      map['thabaal_thogai'] = Variable<double>(thabaalThogai.value);
    }
    if (ahimsaPattuThogai.present) {
      map['ahimsa_pattu_thogai'] = Variable<double>(ahimsaPattuThogai.value);
    }
    if (piravariVugal.present) {
      map['piravari_vugal'] = Variable<String>(piravariVugal.value);
    }
    if (sonthaViruppangal.present) {
      map['sontha_viruppangal'] = Variable<String>(sonthaViruppangal.value);
    }
    if (nibandhanaigal.present) {
      map['nibandhanaigal'] = Variable<String>(nibandhanaigal.value);
    }
    if (ullkurippu.present) {
      map['ullkurippu'] = Variable<String>(ullkurippu.value);
    }
    if (vangiTharavugal.present) {
      map['vangi_tharavugal'] = Variable<String>(vangiTharavugal.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    if (isDeleted.present) {
      map['is_deleted'] = Variable<bool>(isDeleted.value);
    }
    if (deletedAt.present) {
      map['deleted_at'] = Variable<DateTime>(deletedAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('KooliPattiyalTableCompanion(')
          ..write('id: $id, ')
          ..write('niruvanamId: $niruvanamId, ')
          ..write('patrucheettuEn: $patrucheettuEn, ')
          ..write('finYear: $finYear, ')
          ..write('vanakkam: $vanakkam, ')
          ..write('pattiyalVagai: $pattiyalVagai, ')
          ..write('vaangunarId: $vaangunarId, ')
          ..write('vaangunarPeyar: $vaangunarPeyar, ')
          ..write('vaangunarMunvari: $vaangunarMunvari, ')
          ..write('pattiyalNaal: $pattiyalNaal, ')
          ..write('tharavugal: $tharavugal, ')
          ..write('mothaThogai: $mothaThogai, ')
          ..write('thallupadi: $thallupadi, ')
          ..write('podhuThallupadiMathippu: $podhuThallupadiMathippu, ')
          ..write('podhuThallupadiVagai: $podhuThallupadiVagai, ')
          ..write('podhuThallupadiThogai: $podhuThallupadiThogai, ')
          ..write('variThogai: $variThogai, ')
          ..write('variTharavugal: $variTharavugal, ')
          ..write('mothaEdai: $mothaEdai, ')
          ..write('setharamGrams: $setharamGrams, ')
          ..write('thabaalThogai: $thabaalThogai, ')
          ..write('ahimsaPattuThogai: $ahimsaPattuThogai, ')
          ..write('piravariVugal: $piravariVugal, ')
          ..write('sonthaViruppangal: $sonthaViruppangal, ')
          ..write('nibandhanaigal: $nibandhanaigal, ')
          ..write('ullkurippu: $ullkurippu, ')
          ..write('vangiTharavugal: $vangiTharavugal, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }
}

class $KooliPatrugalTableTable extends KooliPatrugalTable
    with TableInfo<$KooliPatrugalTableTable, KooliPatrugalEntry> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $KooliPatrugalTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _niruvanamIdMeta =
      const VerificationMeta('niruvanamId');
  @override
  late final GeneratedColumn<int> niruvanamId = GeneratedColumn<int>(
      'niruvanam_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _patruEnMeta =
      const VerificationMeta('patruEn');
  @override
  late final GeneratedColumn<String> patruEn = GeneratedColumn<String>(
      'patru_en', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: true,
      defaultConstraints: GeneratedColumn.constraintIsAlways('UNIQUE'));
  static const VerificationMeta _finYearMeta =
      const VerificationMeta('finYear');
  @override
  late final GeneratedColumn<String> finYear = GeneratedColumn<String>(
      'fin_year', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _vanakkamMeta =
      const VerificationMeta('vanakkam');
  @override
  late final GeneratedColumn<int> vanakkam = GeneratedColumn<int>(
      'vanakkam', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(1));
  static const VerificationMeta _vaangunarIdMeta =
      const VerificationMeta('vaangunarId');
  @override
  late final GeneratedColumn<int> vaangunarId = GeneratedColumn<int>(
      'vaangunar_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      vaangunarPeyar = GeneratedColumn<String>(
              'vaangunar_peyar', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliPatrugalTableTable.$convertervaangunarPeyar);
  @override
  late final GeneratedColumnWithTypeConverter<Map<String, String>, String>
      vaangunarMunvari = GeneratedColumn<String>(
              'vaangunar_munvari', aliasedName, false,
              type: DriftSqlType.string,
              requiredDuringInsert: false,
              defaultValue: const Constant('{}'))
          .withConverter<Map<String, String>>(
              $KooliPatrugalTableTable.$convertervaangunarMunvari);
  static const VerificationMeta _patruNaalMeta =
      const VerificationMeta('patruNaal');
  @override
  late final GeneratedColumn<DateTime> patruNaal = GeneratedColumn<DateTime>(
      'patru_naal', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _thogaiMeta = const VerificationMeta('thogai');
  @override
  late final GeneratedColumn<double> thogai = GeneratedColumn<double>(
      'thogai', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _seluthumMuraiMeta =
      const VerificationMeta('seluthumMurai');
  @override
  late final GeneratedColumn<String> seluthumMurai = GeneratedColumn<String>(
      'seluthum_murai', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('cash'));
  static const VerificationMeta _vangiPeyarMeta =
      const VerificationMeta('vangiPeyar');
  @override
  late final GeneratedColumn<String> vangiPeyar = GeneratedColumn<String>(
      'vangi_peyar', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _parivarthanaiEnMeta =
      const VerificationMeta('parivarthanaiEn');
  @override
  late final GeneratedColumn<String> parivarthanaiEn = GeneratedColumn<String>(
      'parivarthanai_en', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _ullkurippuMeta =
      const VerificationMeta('ullkurippu');
  @override
  late final GeneratedColumn<String> ullkurippu = GeneratedColumn<String>(
      'ullkurippu', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _updatedAtMeta =
      const VerificationMeta('updatedAt');
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
      'updated_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _isDeletedMeta =
      const VerificationMeta('isDeleted');
  @override
  late final GeneratedColumn<bool> isDeleted = GeneratedColumn<bool>(
      'is_deleted', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_deleted" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _deletedAtMeta =
      const VerificationMeta('deletedAt');
  @override
  late final GeneratedColumn<DateTime> deletedAt = GeneratedColumn<DateTime>(
      'deleted_at', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        niruvanamId,
        patruEn,
        finYear,
        vanakkam,
        vaangunarId,
        vaangunarPeyar,
        vaangunarMunvari,
        patruNaal,
        thogai,
        seluthumMurai,
        vangiPeyar,
        parivarthanaiEn,
        ullkurippu,
        createdAt,
        updatedAt,
        isDeleted,
        deletedAt
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'kooli_patrugal_table';
  @override
  VerificationContext validateIntegrity(Insertable<KooliPatrugalEntry> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('niruvanam_id')) {
      context.handle(
          _niruvanamIdMeta,
          niruvanamId.isAcceptableOrUnknown(
              data['niruvanam_id']!, _niruvanamIdMeta));
    }
    if (data.containsKey('patru_en')) {
      context.handle(_patruEnMeta,
          patruEn.isAcceptableOrUnknown(data['patru_en']!, _patruEnMeta));
    } else if (isInserting) {
      context.missing(_patruEnMeta);
    }
    if (data.containsKey('fin_year')) {
      context.handle(_finYearMeta,
          finYear.isAcceptableOrUnknown(data['fin_year']!, _finYearMeta));
    }
    if (data.containsKey('vanakkam')) {
      context.handle(_vanakkamMeta,
          vanakkam.isAcceptableOrUnknown(data['vanakkam']!, _vanakkamMeta));
    }
    if (data.containsKey('vaangunar_id')) {
      context.handle(
          _vaangunarIdMeta,
          vaangunarId.isAcceptableOrUnknown(
              data['vaangunar_id']!, _vaangunarIdMeta));
    }
    if (data.containsKey('patru_naal')) {
      context.handle(_patruNaalMeta,
          patruNaal.isAcceptableOrUnknown(data['patru_naal']!, _patruNaalMeta));
    }
    if (data.containsKey('thogai')) {
      context.handle(_thogaiMeta,
          thogai.isAcceptableOrUnknown(data['thogai']!, _thogaiMeta));
    }
    if (data.containsKey('seluthum_murai')) {
      context.handle(
          _seluthumMuraiMeta,
          seluthumMurai.isAcceptableOrUnknown(
              data['seluthum_murai']!, _seluthumMuraiMeta));
    }
    if (data.containsKey('vangi_peyar')) {
      context.handle(
          _vangiPeyarMeta,
          vangiPeyar.isAcceptableOrUnknown(
              data['vangi_peyar']!, _vangiPeyarMeta));
    }
    if (data.containsKey('parivarthanai_en')) {
      context.handle(
          _parivarthanaiEnMeta,
          parivarthanaiEn.isAcceptableOrUnknown(
              data['parivarthanai_en']!, _parivarthanaiEnMeta));
    }
    if (data.containsKey('ullkurippu')) {
      context.handle(
          _ullkurippuMeta,
          ullkurippu.isAcceptableOrUnknown(
              data['ullkurippu']!, _ullkurippuMeta));
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    if (data.containsKey('updated_at')) {
      context.handle(_updatedAtMeta,
          updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta));
    }
    if (data.containsKey('is_deleted')) {
      context.handle(_isDeletedMeta,
          isDeleted.isAcceptableOrUnknown(data['is_deleted']!, _isDeletedMeta));
    }
    if (data.containsKey('deleted_at')) {
      context.handle(_deletedAtMeta,
          deletedAt.isAcceptableOrUnknown(data['deleted_at']!, _deletedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  List<Set<GeneratedColumn>> get uniqueKeys => [
        {niruvanamId, finYear, vanakkam},
      ];
  @override
  KooliPatrugalEntry map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return KooliPatrugalEntry(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      niruvanamId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}niruvanam_id']),
      patruEn: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}patru_en'])!,
      finYear: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}fin_year'])!,
      vanakkam: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}vanakkam'])!,
      vaangunarId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}vaangunar_id']),
      vaangunarPeyar: $KooliPatrugalTableTable.$convertervaangunarPeyar.fromSql(
          attachedDatabase.typeMapping.read(
              DriftSqlType.string, data['${effectivePrefix}vaangunar_peyar'])!),
      vaangunarMunvari: $KooliPatrugalTableTable.$convertervaangunarMunvari
          .fromSql(attachedDatabase.typeMapping.read(DriftSqlType.string,
              data['${effectivePrefix}vaangunar_munvari'])!),
      patruNaal: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}patru_naal'])!,
      thogai: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}thogai'])!,
      seluthumMurai: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}seluthum_murai'])!,
      vangiPeyar: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}vangi_peyar']),
      parivarthanaiEn: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}parivarthanai_en']),
      ullkurippu: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}ullkurippu'])!,
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
      updatedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated_at'])!,
      isDeleted: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_deleted'])!,
      deletedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}deleted_at']),
    );
  }

  @override
  $KooliPatrugalTableTable createAlias(String alias) {
    return $KooliPatrugalTableTable(attachedDatabase, alias);
  }

  static TypeConverter<Map<String, String>, String> $convertervaangunarPeyar =
      const MozhiMapConverter();
  static TypeConverter<Map<String, String>, String> $convertervaangunarMunvari =
      const MozhiMapConverter();
}

class KooliPatrugalEntry extends DataClass
    implements Insertable<KooliPatrugalEntry> {
  final int id;
  final int? niruvanamId;
  final String patruEn;
  final String finYear;
  final int vanakkam;
  final int? vaangunarId;
  final Map<String, String> vaangunarPeyar;
  final Map<String, String> vaangunarMunvari;
  final DateTime patruNaal;
  final double thogai;
  final String seluthumMurai;
  final String? vangiPeyar;
  final String? parivarthanaiEn;
  final String ullkurippu;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isDeleted;
  final DateTime? deletedAt;
  const KooliPatrugalEntry(
      {required this.id,
      this.niruvanamId,
      required this.patruEn,
      required this.finYear,
      required this.vanakkam,
      this.vaangunarId,
      required this.vaangunarPeyar,
      required this.vaangunarMunvari,
      required this.patruNaal,
      required this.thogai,
      required this.seluthumMurai,
      this.vangiPeyar,
      this.parivarthanaiEn,
      required this.ullkurippu,
      required this.createdAt,
      required this.updatedAt,
      required this.isDeleted,
      this.deletedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    if (!nullToAbsent || niruvanamId != null) {
      map['niruvanam_id'] = Variable<int>(niruvanamId);
    }
    map['patru_en'] = Variable<String>(patruEn);
    map['fin_year'] = Variable<String>(finYear);
    map['vanakkam'] = Variable<int>(vanakkam);
    if (!nullToAbsent || vaangunarId != null) {
      map['vaangunar_id'] = Variable<int>(vaangunarId);
    }
    {
      map['vaangunar_peyar'] = Variable<String>($KooliPatrugalTableTable
          .$convertervaangunarPeyar
          .toSql(vaangunarPeyar));
    }
    {
      map['vaangunar_munvari'] = Variable<String>($KooliPatrugalTableTable
          .$convertervaangunarMunvari
          .toSql(vaangunarMunvari));
    }
    map['patru_naal'] = Variable<DateTime>(patruNaal);
    map['thogai'] = Variable<double>(thogai);
    map['seluthum_murai'] = Variable<String>(seluthumMurai);
    if (!nullToAbsent || vangiPeyar != null) {
      map['vangi_peyar'] = Variable<String>(vangiPeyar);
    }
    if (!nullToAbsent || parivarthanaiEn != null) {
      map['parivarthanai_en'] = Variable<String>(parivarthanaiEn);
    }
    map['ullkurippu'] = Variable<String>(ullkurippu);
    map['created_at'] = Variable<DateTime>(createdAt);
    map['updated_at'] = Variable<DateTime>(updatedAt);
    map['is_deleted'] = Variable<bool>(isDeleted);
    if (!nullToAbsent || deletedAt != null) {
      map['deleted_at'] = Variable<DateTime>(deletedAt);
    }
    return map;
  }

  KooliPatrugalTableCompanion toCompanion(bool nullToAbsent) {
    return KooliPatrugalTableCompanion(
      id: Value(id),
      niruvanamId: niruvanamId == null && nullToAbsent
          ? const Value.absent()
          : Value(niruvanamId),
      patruEn: Value(patruEn),
      finYear: Value(finYear),
      vanakkam: Value(vanakkam),
      vaangunarId: vaangunarId == null && nullToAbsent
          ? const Value.absent()
          : Value(vaangunarId),
      vaangunarPeyar: Value(vaangunarPeyar),
      vaangunarMunvari: Value(vaangunarMunvari),
      patruNaal: Value(patruNaal),
      thogai: Value(thogai),
      seluthumMurai: Value(seluthumMurai),
      vangiPeyar: vangiPeyar == null && nullToAbsent
          ? const Value.absent()
          : Value(vangiPeyar),
      parivarthanaiEn: parivarthanaiEn == null && nullToAbsent
          ? const Value.absent()
          : Value(parivarthanaiEn),
      ullkurippu: Value(ullkurippu),
      createdAt: Value(createdAt),
      updatedAt: Value(updatedAt),
      isDeleted: Value(isDeleted),
      deletedAt: deletedAt == null && nullToAbsent
          ? const Value.absent()
          : Value(deletedAt),
    );
  }

  factory KooliPatrugalEntry.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return KooliPatrugalEntry(
      id: serializer.fromJson<int>(json['id']),
      niruvanamId: serializer.fromJson<int?>(json['niruvanamId']),
      patruEn: serializer.fromJson<String>(json['patruEn']),
      finYear: serializer.fromJson<String>(json['finYear']),
      vanakkam: serializer.fromJson<int>(json['vanakkam']),
      vaangunarId: serializer.fromJson<int?>(json['vaangunarId']),
      vaangunarPeyar:
          serializer.fromJson<Map<String, String>>(json['vaangunarPeyar']),
      vaangunarMunvari:
          serializer.fromJson<Map<String, String>>(json['vaangunarMunvari']),
      patruNaal: serializer.fromJson<DateTime>(json['patruNaal']),
      thogai: serializer.fromJson<double>(json['thogai']),
      seluthumMurai: serializer.fromJson<String>(json['seluthumMurai']),
      vangiPeyar: serializer.fromJson<String?>(json['vangiPeyar']),
      parivarthanaiEn: serializer.fromJson<String?>(json['parivarthanaiEn']),
      ullkurippu: serializer.fromJson<String>(json['ullkurippu']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
      isDeleted: serializer.fromJson<bool>(json['isDeleted']),
      deletedAt: serializer.fromJson<DateTime?>(json['deletedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'niruvanamId': serializer.toJson<int?>(niruvanamId),
      'patruEn': serializer.toJson<String>(patruEn),
      'finYear': serializer.toJson<String>(finYear),
      'vanakkam': serializer.toJson<int>(vanakkam),
      'vaangunarId': serializer.toJson<int?>(vaangunarId),
      'vaangunarPeyar': serializer.toJson<Map<String, String>>(vaangunarPeyar),
      'vaangunarMunvari':
          serializer.toJson<Map<String, String>>(vaangunarMunvari),
      'patruNaal': serializer.toJson<DateTime>(patruNaal),
      'thogai': serializer.toJson<double>(thogai),
      'seluthumMurai': serializer.toJson<String>(seluthumMurai),
      'vangiPeyar': serializer.toJson<String?>(vangiPeyar),
      'parivarthanaiEn': serializer.toJson<String?>(parivarthanaiEn),
      'ullkurippu': serializer.toJson<String>(ullkurippu),
      'createdAt': serializer.toJson<DateTime>(createdAt),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
      'isDeleted': serializer.toJson<bool>(isDeleted),
      'deletedAt': serializer.toJson<DateTime?>(deletedAt),
    };
  }

  KooliPatrugalEntry copyWith(
          {int? id,
          Value<int?> niruvanamId = const Value.absent(),
          String? patruEn,
          String? finYear,
          int? vanakkam,
          Value<int?> vaangunarId = const Value.absent(),
          Map<String, String>? vaangunarPeyar,
          Map<String, String>? vaangunarMunvari,
          DateTime? patruNaal,
          double? thogai,
          String? seluthumMurai,
          Value<String?> vangiPeyar = const Value.absent(),
          Value<String?> parivarthanaiEn = const Value.absent(),
          String? ullkurippu,
          DateTime? createdAt,
          DateTime? updatedAt,
          bool? isDeleted,
          Value<DateTime?> deletedAt = const Value.absent()}) =>
      KooliPatrugalEntry(
        id: id ?? this.id,
        niruvanamId: niruvanamId.present ? niruvanamId.value : this.niruvanamId,
        patruEn: patruEn ?? this.patruEn,
        finYear: finYear ?? this.finYear,
        vanakkam: vanakkam ?? this.vanakkam,
        vaangunarId: vaangunarId.present ? vaangunarId.value : this.vaangunarId,
        vaangunarPeyar: vaangunarPeyar ?? this.vaangunarPeyar,
        vaangunarMunvari: vaangunarMunvari ?? this.vaangunarMunvari,
        patruNaal: patruNaal ?? this.patruNaal,
        thogai: thogai ?? this.thogai,
        seluthumMurai: seluthumMurai ?? this.seluthumMurai,
        vangiPeyar: vangiPeyar.present ? vangiPeyar.value : this.vangiPeyar,
        parivarthanaiEn: parivarthanaiEn.present
            ? parivarthanaiEn.value
            : this.parivarthanaiEn,
        ullkurippu: ullkurippu ?? this.ullkurippu,
        createdAt: createdAt ?? this.createdAt,
        updatedAt: updatedAt ?? this.updatedAt,
        isDeleted: isDeleted ?? this.isDeleted,
        deletedAt: deletedAt.present ? deletedAt.value : this.deletedAt,
      );
  KooliPatrugalEntry copyWithCompanion(KooliPatrugalTableCompanion data) {
    return KooliPatrugalEntry(
      id: data.id.present ? data.id.value : this.id,
      niruvanamId:
          data.niruvanamId.present ? data.niruvanamId.value : this.niruvanamId,
      patruEn: data.patruEn.present ? data.patruEn.value : this.patruEn,
      finYear: data.finYear.present ? data.finYear.value : this.finYear,
      vanakkam: data.vanakkam.present ? data.vanakkam.value : this.vanakkam,
      vaangunarId:
          data.vaangunarId.present ? data.vaangunarId.value : this.vaangunarId,
      vaangunarPeyar: data.vaangunarPeyar.present
          ? data.vaangunarPeyar.value
          : this.vaangunarPeyar,
      vaangunarMunvari: data.vaangunarMunvari.present
          ? data.vaangunarMunvari.value
          : this.vaangunarMunvari,
      patruNaal: data.patruNaal.present ? data.patruNaal.value : this.patruNaal,
      thogai: data.thogai.present ? data.thogai.value : this.thogai,
      seluthumMurai: data.seluthumMurai.present
          ? data.seluthumMurai.value
          : this.seluthumMurai,
      vangiPeyar:
          data.vangiPeyar.present ? data.vangiPeyar.value : this.vangiPeyar,
      parivarthanaiEn: data.parivarthanaiEn.present
          ? data.parivarthanaiEn.value
          : this.parivarthanaiEn,
      ullkurippu:
          data.ullkurippu.present ? data.ullkurippu.value : this.ullkurippu,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
      isDeleted: data.isDeleted.present ? data.isDeleted.value : this.isDeleted,
      deletedAt: data.deletedAt.present ? data.deletedAt.value : this.deletedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('KooliPatrugalEntry(')
          ..write('id: $id, ')
          ..write('niruvanamId: $niruvanamId, ')
          ..write('patruEn: $patruEn, ')
          ..write('finYear: $finYear, ')
          ..write('vanakkam: $vanakkam, ')
          ..write('vaangunarId: $vaangunarId, ')
          ..write('vaangunarPeyar: $vaangunarPeyar, ')
          ..write('vaangunarMunvari: $vaangunarMunvari, ')
          ..write('patruNaal: $patruNaal, ')
          ..write('thogai: $thogai, ')
          ..write('seluthumMurai: $seluthumMurai, ')
          ..write('vangiPeyar: $vangiPeyar, ')
          ..write('parivarthanaiEn: $parivarthanaiEn, ')
          ..write('ullkurippu: $ullkurippu, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id,
      niruvanamId,
      patruEn,
      finYear,
      vanakkam,
      vaangunarId,
      vaangunarPeyar,
      vaangunarMunvari,
      patruNaal,
      thogai,
      seluthumMurai,
      vangiPeyar,
      parivarthanaiEn,
      ullkurippu,
      createdAt,
      updatedAt,
      isDeleted,
      deletedAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is KooliPatrugalEntry &&
          other.id == this.id &&
          other.niruvanamId == this.niruvanamId &&
          other.patruEn == this.patruEn &&
          other.finYear == this.finYear &&
          other.vanakkam == this.vanakkam &&
          other.vaangunarId == this.vaangunarId &&
          other.vaangunarPeyar == this.vaangunarPeyar &&
          other.vaangunarMunvari == this.vaangunarMunvari &&
          other.patruNaal == this.patruNaal &&
          other.thogai == this.thogai &&
          other.seluthumMurai == this.seluthumMurai &&
          other.vangiPeyar == this.vangiPeyar &&
          other.parivarthanaiEn == this.parivarthanaiEn &&
          other.ullkurippu == this.ullkurippu &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt &&
          other.isDeleted == this.isDeleted &&
          other.deletedAt == this.deletedAt);
}

class KooliPatrugalTableCompanion extends UpdateCompanion<KooliPatrugalEntry> {
  final Value<int> id;
  final Value<int?> niruvanamId;
  final Value<String> patruEn;
  final Value<String> finYear;
  final Value<int> vanakkam;
  final Value<int?> vaangunarId;
  final Value<Map<String, String>> vaangunarPeyar;
  final Value<Map<String, String>> vaangunarMunvari;
  final Value<DateTime> patruNaal;
  final Value<double> thogai;
  final Value<String> seluthumMurai;
  final Value<String?> vangiPeyar;
  final Value<String?> parivarthanaiEn;
  final Value<String> ullkurippu;
  final Value<DateTime> createdAt;
  final Value<DateTime> updatedAt;
  final Value<bool> isDeleted;
  final Value<DateTime?> deletedAt;
  const KooliPatrugalTableCompanion({
    this.id = const Value.absent(),
    this.niruvanamId = const Value.absent(),
    this.patruEn = const Value.absent(),
    this.finYear = const Value.absent(),
    this.vanakkam = const Value.absent(),
    this.vaangunarId = const Value.absent(),
    this.vaangunarPeyar = const Value.absent(),
    this.vaangunarMunvari = const Value.absent(),
    this.patruNaal = const Value.absent(),
    this.thogai = const Value.absent(),
    this.seluthumMurai = const Value.absent(),
    this.vangiPeyar = const Value.absent(),
    this.parivarthanaiEn = const Value.absent(),
    this.ullkurippu = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  });
  KooliPatrugalTableCompanion.insert({
    this.id = const Value.absent(),
    this.niruvanamId = const Value.absent(),
    required String patruEn,
    this.finYear = const Value.absent(),
    this.vanakkam = const Value.absent(),
    this.vaangunarId = const Value.absent(),
    this.vaangunarPeyar = const Value.absent(),
    this.vaangunarMunvari = const Value.absent(),
    this.patruNaal = const Value.absent(),
    this.thogai = const Value.absent(),
    this.seluthumMurai = const Value.absent(),
    this.vangiPeyar = const Value.absent(),
    this.parivarthanaiEn = const Value.absent(),
    this.ullkurippu = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.isDeleted = const Value.absent(),
    this.deletedAt = const Value.absent(),
  }) : patruEn = Value(patruEn);
  static Insertable<KooliPatrugalEntry> custom({
    Expression<int>? id,
    Expression<int>? niruvanamId,
    Expression<String>? patruEn,
    Expression<String>? finYear,
    Expression<int>? vanakkam,
    Expression<int>? vaangunarId,
    Expression<String>? vaangunarPeyar,
    Expression<String>? vaangunarMunvari,
    Expression<DateTime>? patruNaal,
    Expression<double>? thogai,
    Expression<String>? seluthumMurai,
    Expression<String>? vangiPeyar,
    Expression<String>? parivarthanaiEn,
    Expression<String>? ullkurippu,
    Expression<DateTime>? createdAt,
    Expression<DateTime>? updatedAt,
    Expression<bool>? isDeleted,
    Expression<DateTime>? deletedAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (niruvanamId != null) 'niruvanam_id': niruvanamId,
      if (patruEn != null) 'patru_en': patruEn,
      if (finYear != null) 'fin_year': finYear,
      if (vanakkam != null) 'vanakkam': vanakkam,
      if (vaangunarId != null) 'vaangunar_id': vaangunarId,
      if (vaangunarPeyar != null) 'vaangunar_peyar': vaangunarPeyar,
      if (vaangunarMunvari != null) 'vaangunar_munvari': vaangunarMunvari,
      if (patruNaal != null) 'patru_naal': patruNaal,
      if (thogai != null) 'thogai': thogai,
      if (seluthumMurai != null) 'seluthum_murai': seluthumMurai,
      if (vangiPeyar != null) 'vangi_peyar': vangiPeyar,
      if (parivarthanaiEn != null) 'parivarthanai_en': parivarthanaiEn,
      if (ullkurippu != null) 'ullkurippu': ullkurippu,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (isDeleted != null) 'is_deleted': isDeleted,
      if (deletedAt != null) 'deleted_at': deletedAt,
    });
  }

  KooliPatrugalTableCompanion copyWith(
      {Value<int>? id,
      Value<int?>? niruvanamId,
      Value<String>? patruEn,
      Value<String>? finYear,
      Value<int>? vanakkam,
      Value<int?>? vaangunarId,
      Value<Map<String, String>>? vaangunarPeyar,
      Value<Map<String, String>>? vaangunarMunvari,
      Value<DateTime>? patruNaal,
      Value<double>? thogai,
      Value<String>? seluthumMurai,
      Value<String?>? vangiPeyar,
      Value<String?>? parivarthanaiEn,
      Value<String>? ullkurippu,
      Value<DateTime>? createdAt,
      Value<DateTime>? updatedAt,
      Value<bool>? isDeleted,
      Value<DateTime?>? deletedAt}) {
    return KooliPatrugalTableCompanion(
      id: id ?? this.id,
      niruvanamId: niruvanamId ?? this.niruvanamId,
      patruEn: patruEn ?? this.patruEn,
      finYear: finYear ?? this.finYear,
      vanakkam: vanakkam ?? this.vanakkam,
      vaangunarId: vaangunarId ?? this.vaangunarId,
      vaangunarPeyar: vaangunarPeyar ?? this.vaangunarPeyar,
      vaangunarMunvari: vaangunarMunvari ?? this.vaangunarMunvari,
      patruNaal: patruNaal ?? this.patruNaal,
      thogai: thogai ?? this.thogai,
      seluthumMurai: seluthumMurai ?? this.seluthumMurai,
      vangiPeyar: vangiPeyar ?? this.vangiPeyar,
      parivarthanaiEn: parivarthanaiEn ?? this.parivarthanaiEn,
      ullkurippu: ullkurippu ?? this.ullkurippu,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      isDeleted: isDeleted ?? this.isDeleted,
      deletedAt: deletedAt ?? this.deletedAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (niruvanamId.present) {
      map['niruvanam_id'] = Variable<int>(niruvanamId.value);
    }
    if (patruEn.present) {
      map['patru_en'] = Variable<String>(patruEn.value);
    }
    if (finYear.present) {
      map['fin_year'] = Variable<String>(finYear.value);
    }
    if (vanakkam.present) {
      map['vanakkam'] = Variable<int>(vanakkam.value);
    }
    if (vaangunarId.present) {
      map['vaangunar_id'] = Variable<int>(vaangunarId.value);
    }
    if (vaangunarPeyar.present) {
      map['vaangunar_peyar'] = Variable<String>($KooliPatrugalTableTable
          .$convertervaangunarPeyar
          .toSql(vaangunarPeyar.value));
    }
    if (vaangunarMunvari.present) {
      map['vaangunar_munvari'] = Variable<String>($KooliPatrugalTableTable
          .$convertervaangunarMunvari
          .toSql(vaangunarMunvari.value));
    }
    if (patruNaal.present) {
      map['patru_naal'] = Variable<DateTime>(patruNaal.value);
    }
    if (thogai.present) {
      map['thogai'] = Variable<double>(thogai.value);
    }
    if (seluthumMurai.present) {
      map['seluthum_murai'] = Variable<String>(seluthumMurai.value);
    }
    if (vangiPeyar.present) {
      map['vangi_peyar'] = Variable<String>(vangiPeyar.value);
    }
    if (parivarthanaiEn.present) {
      map['parivarthanai_en'] = Variable<String>(parivarthanaiEn.value);
    }
    if (ullkurippu.present) {
      map['ullkurippu'] = Variable<String>(ullkurippu.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    if (isDeleted.present) {
      map['is_deleted'] = Variable<bool>(isDeleted.value);
    }
    if (deletedAt.present) {
      map['deleted_at'] = Variable<DateTime>(deletedAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('KooliPatrugalTableCompanion(')
          ..write('id: $id, ')
          ..write('niruvanamId: $niruvanamId, ')
          ..write('patruEn: $patruEn, ')
          ..write('finYear: $finYear, ')
          ..write('vanakkam: $vanakkam, ')
          ..write('vaangunarId: $vaangunarId, ')
          ..write('vaangunarPeyar: $vaangunarPeyar, ')
          ..write('vaangunarMunvari: $vaangunarMunvari, ')
          ..write('patruNaal: $patruNaal, ')
          ..write('thogai: $thogai, ')
          ..write('seluthumMurai: $seluthumMurai, ')
          ..write('vangiPeyar: $vangiPeyar, ')
          ..write('parivarthanaiEn: $parivarthanaiEn, ')
          ..write('ullkurippu: $ullkurippu, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('isDeleted: $isDeleted, ')
          ..write('deletedAt: $deletedAt')
          ..write(')'))
        .toString();
  }
}

class $KooliPatruPattiyalTableTable extends KooliPatruPattiyalTable
    with TableInfo<$KooliPatruPattiyalTableTable, KooliPatruPattiyalEntry> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $KooliPatruPattiyalTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _patruIdMeta =
      const VerificationMeta('patruId');
  @override
  late final GeneratedColumn<int> patruId = GeneratedColumn<int>(
      'patru_id', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _pattiyalIdMeta =
      const VerificationMeta('pattiyalId');
  @override
  late final GeneratedColumn<int> pattiyalId = GeneratedColumn<int>(
      'pattiyal_id', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _poruthiyaThogaiMeta =
      const VerificationMeta('poruthiyaThogai');
  @override
  late final GeneratedColumn<double> poruthiyaThogai = GeneratedColumn<double>(
      'poruthiya_thogai', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  @override
  List<GeneratedColumn> get $columns =>
      [id, patruId, pattiyalId, poruthiyaThogai];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'kooli_patru_pattiyal_table';
  @override
  VerificationContext validateIntegrity(
      Insertable<KooliPatruPattiyalEntry> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('patru_id')) {
      context.handle(_patruIdMeta,
          patruId.isAcceptableOrUnknown(data['patru_id']!, _patruIdMeta));
    } else if (isInserting) {
      context.missing(_patruIdMeta);
    }
    if (data.containsKey('pattiyal_id')) {
      context.handle(
          _pattiyalIdMeta,
          pattiyalId.isAcceptableOrUnknown(
              data['pattiyal_id']!, _pattiyalIdMeta));
    } else if (isInserting) {
      context.missing(_pattiyalIdMeta);
    }
    if (data.containsKey('poruthiya_thogai')) {
      context.handle(
          _poruthiyaThogaiMeta,
          poruthiyaThogai.isAcceptableOrUnknown(
              data['poruthiya_thogai']!, _poruthiyaThogaiMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  KooliPatruPattiyalEntry map(Map<String, dynamic> data,
      {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return KooliPatruPattiyalEntry(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      patruId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}patru_id'])!,
      pattiyalId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}pattiyal_id'])!,
      poruthiyaThogai: attachedDatabase.typeMapping.read(
          DriftSqlType.double, data['${effectivePrefix}poruthiya_thogai'])!,
    );
  }

  @override
  $KooliPatruPattiyalTableTable createAlias(String alias) {
    return $KooliPatruPattiyalTableTable(attachedDatabase, alias);
  }
}

class KooliPatruPattiyalEntry extends DataClass
    implements Insertable<KooliPatruPattiyalEntry> {
  final int id;
  final int patruId;
  final int pattiyalId;
  final double poruthiyaThogai;
  const KooliPatruPattiyalEntry(
      {required this.id,
      required this.patruId,
      required this.pattiyalId,
      required this.poruthiyaThogai});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['patru_id'] = Variable<int>(patruId);
    map['pattiyal_id'] = Variable<int>(pattiyalId);
    map['poruthiya_thogai'] = Variable<double>(poruthiyaThogai);
    return map;
  }

  KooliPatruPattiyalTableCompanion toCompanion(bool nullToAbsent) {
    return KooliPatruPattiyalTableCompanion(
      id: Value(id),
      patruId: Value(patruId),
      pattiyalId: Value(pattiyalId),
      poruthiyaThogai: Value(poruthiyaThogai),
    );
  }

  factory KooliPatruPattiyalEntry.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return KooliPatruPattiyalEntry(
      id: serializer.fromJson<int>(json['id']),
      patruId: serializer.fromJson<int>(json['patruId']),
      pattiyalId: serializer.fromJson<int>(json['pattiyalId']),
      poruthiyaThogai: serializer.fromJson<double>(json['poruthiyaThogai']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'patruId': serializer.toJson<int>(patruId),
      'pattiyalId': serializer.toJson<int>(pattiyalId),
      'poruthiyaThogai': serializer.toJson<double>(poruthiyaThogai),
    };
  }

  KooliPatruPattiyalEntry copyWith(
          {int? id, int? patruId, int? pattiyalId, double? poruthiyaThogai}) =>
      KooliPatruPattiyalEntry(
        id: id ?? this.id,
        patruId: patruId ?? this.patruId,
        pattiyalId: pattiyalId ?? this.pattiyalId,
        poruthiyaThogai: poruthiyaThogai ?? this.poruthiyaThogai,
      );
  KooliPatruPattiyalEntry copyWithCompanion(
      KooliPatruPattiyalTableCompanion data) {
    return KooliPatruPattiyalEntry(
      id: data.id.present ? data.id.value : this.id,
      patruId: data.patruId.present ? data.patruId.value : this.patruId,
      pattiyalId:
          data.pattiyalId.present ? data.pattiyalId.value : this.pattiyalId,
      poruthiyaThogai: data.poruthiyaThogai.present
          ? data.poruthiyaThogai.value
          : this.poruthiyaThogai,
    );
  }

  @override
  String toString() {
    return (StringBuffer('KooliPatruPattiyalEntry(')
          ..write('id: $id, ')
          ..write('patruId: $patruId, ')
          ..write('pattiyalId: $pattiyalId, ')
          ..write('poruthiyaThogai: $poruthiyaThogai')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, patruId, pattiyalId, poruthiyaThogai);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is KooliPatruPattiyalEntry &&
          other.id == this.id &&
          other.patruId == this.patruId &&
          other.pattiyalId == this.pattiyalId &&
          other.poruthiyaThogai == this.poruthiyaThogai);
}

class KooliPatruPattiyalTableCompanion
    extends UpdateCompanion<KooliPatruPattiyalEntry> {
  final Value<int> id;
  final Value<int> patruId;
  final Value<int> pattiyalId;
  final Value<double> poruthiyaThogai;
  const KooliPatruPattiyalTableCompanion({
    this.id = const Value.absent(),
    this.patruId = const Value.absent(),
    this.pattiyalId = const Value.absent(),
    this.poruthiyaThogai = const Value.absent(),
  });
  KooliPatruPattiyalTableCompanion.insert({
    this.id = const Value.absent(),
    required int patruId,
    required int pattiyalId,
    this.poruthiyaThogai = const Value.absent(),
  })  : patruId = Value(patruId),
        pattiyalId = Value(pattiyalId);
  static Insertable<KooliPatruPattiyalEntry> custom({
    Expression<int>? id,
    Expression<int>? patruId,
    Expression<int>? pattiyalId,
    Expression<double>? poruthiyaThogai,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (patruId != null) 'patru_id': patruId,
      if (pattiyalId != null) 'pattiyal_id': pattiyalId,
      if (poruthiyaThogai != null) 'poruthiya_thogai': poruthiyaThogai,
    });
  }

  KooliPatruPattiyalTableCompanion copyWith(
      {Value<int>? id,
      Value<int>? patruId,
      Value<int>? pattiyalId,
      Value<double>? poruthiyaThogai}) {
    return KooliPatruPattiyalTableCompanion(
      id: id ?? this.id,
      patruId: patruId ?? this.patruId,
      pattiyalId: pattiyalId ?? this.pattiyalId,
      poruthiyaThogai: poruthiyaThogai ?? this.poruthiyaThogai,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (patruId.present) {
      map['patru_id'] = Variable<int>(patruId.value);
    }
    if (pattiyalId.present) {
      map['pattiyal_id'] = Variable<int>(pattiyalId.value);
    }
    if (poruthiyaThogai.present) {
      map['poruthiya_thogai'] = Variable<double>(poruthiyaThogai.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('KooliPatruPattiyalTableCompanion(')
          ..write('id: $id, ')
          ..write('patruId: $patruId, ')
          ..write('pattiyalId: $pattiyalId, ')
          ..write('poruthiyaThogai: $poruthiyaThogai')
          ..write(')'))
        .toString();
  }
}

abstract class _$KooliDatabase extends GeneratedDatabase {
  _$KooliDatabase(QueryExecutor e) : super(e);
  $KooliDatabaseManager get managers => $KooliDatabaseManager(this);
  late final $KooliNiruvanaTharavugalTableTable kooliNiruvanaTharavugalTable =
      $KooliNiruvanaTharavugalTableTable(this);
  late final $KooliVaangunarTableTable kooliVaangunarTable =
      $KooliVaangunarTableTable(this);
  late final $KooliPorulTableTable kooliPorulTable =
      $KooliPorulTableTable(this);
  late final $KooliPattiyalTableTable kooliPattiyalTable =
      $KooliPattiyalTableTable(this);
  late final $KooliPatrugalTableTable kooliPatrugalTable =
      $KooliPatrugalTableTable(this);
  late final $KooliPatruPattiyalTableTable kooliPatruPattiyalTable =
      $KooliPatruPattiyalTableTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
        kooliNiruvanaTharavugalTable,
        kooliVaangunarTable,
        kooliPorulTable,
        kooliPattiyalTable,
        kooliPatrugalTable,
        kooliPatruPattiyalTable
      ];
}

typedef $$KooliNiruvanaTharavugalTableTableCreateCompanionBuilder
    = KooliNiruvanaTharavugalTableCompanion Function({
  Value<int> id,
  Value<Map<String, String>> niruvanathinPeyar,
  Value<String> kurumPeyar,
  Value<String> tholaipaesi1,
  Value<String> tholaipaesi2,
  Value<String> minnanjal,
  Value<String> gstin,
  Value<Map<String, String>> mugavari,
  Value<Map<String, String>> oor,
  Value<Map<String, String>> maavattam,
  Value<Map<String, String>> maanilam,
  Value<Map<String, String>> naadu,
  Value<String> anjalKuriyeedu,
  Value<Map<String, String>> vangiPeyar,
  Value<Map<String, String>> kilai,
  Value<String> vangiKanakku,
  Value<String> ifsc,
  Value<String> oavuru,
  Value<String> agalaOavuru,
  Value<String> thalaippuVadivu,
  Value<String> kaiyoppam,
  Value<String> oppamPeyar,
  Value<Map<String, String>> adaimozhi,
  Value<String> upiId,
  Value<String> thoatraNiram,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});
typedef $$KooliNiruvanaTharavugalTableTableUpdateCompanionBuilder
    = KooliNiruvanaTharavugalTableCompanion Function({
  Value<int> id,
  Value<Map<String, String>> niruvanathinPeyar,
  Value<String> kurumPeyar,
  Value<String> tholaipaesi1,
  Value<String> tholaipaesi2,
  Value<String> minnanjal,
  Value<String> gstin,
  Value<Map<String, String>> mugavari,
  Value<Map<String, String>> oor,
  Value<Map<String, String>> maavattam,
  Value<Map<String, String>> maanilam,
  Value<Map<String, String>> naadu,
  Value<String> anjalKuriyeedu,
  Value<Map<String, String>> vangiPeyar,
  Value<Map<String, String>> kilai,
  Value<String> vangiKanakku,
  Value<String> ifsc,
  Value<String> oavuru,
  Value<String> agalaOavuru,
  Value<String> thalaippuVadivu,
  Value<String> kaiyoppam,
  Value<String> oppamPeyar,
  Value<Map<String, String>> adaimozhi,
  Value<String> upiId,
  Value<String> thoatraNiram,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});

class $$KooliNiruvanaTharavugalTableTableFilterComposer
    extends Composer<_$KooliDatabase, $KooliNiruvanaTharavugalTableTable> {
  $$KooliNiruvanaTharavugalTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get niruvanathinPeyar => $composableBuilder(
          column: $table.niruvanathinPeyar,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnFilters<String> get kurumPeyar => $composableBuilder(
      column: $table.kurumPeyar, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get tholaipaesi1 => $composableBuilder(
      column: $table.tholaipaesi1, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get tholaipaesi2 => $composableBuilder(
      column: $table.tholaipaesi2, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get minnanjal => $composableBuilder(
      column: $table.minnanjal, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get gstin => $composableBuilder(
      column: $table.gstin, builder: (column) => ColumnFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get mugavari => $composableBuilder(
          column: $table.mugavari,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get oor => $composableBuilder(
          column: $table.oor,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get maavattam => $composableBuilder(
          column: $table.maavattam,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get maanilam => $composableBuilder(
          column: $table.maanilam,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get naadu => $composableBuilder(
          column: $table.naadu,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnFilters<String> get anjalKuriyeedu => $composableBuilder(
      column: $table.anjalKuriyeedu,
      builder: (column) => ColumnFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get vangiPeyar => $composableBuilder(
          column: $table.vangiPeyar,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get kilai => $composableBuilder(
          column: $table.kilai,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnFilters<String> get vangiKanakku => $composableBuilder(
      column: $table.vangiKanakku, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get ifsc => $composableBuilder(
      column: $table.ifsc, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get oavuru => $composableBuilder(
      column: $table.oavuru, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get agalaOavuru => $composableBuilder(
      column: $table.agalaOavuru, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get thalaippuVadivu => $composableBuilder(
      column: $table.thalaippuVadivu,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get kaiyoppam => $composableBuilder(
      column: $table.kaiyoppam, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get oppamPeyar => $composableBuilder(
      column: $table.oppamPeyar, builder: (column) => ColumnFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get adaimozhi => $composableBuilder(
          column: $table.adaimozhi,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnFilters<String> get upiId => $composableBuilder(
      column: $table.upiId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get thoatraNiram => $composableBuilder(
      column: $table.thoatraNiram, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnFilters(column));
}

class $$KooliNiruvanaTharavugalTableTableOrderingComposer
    extends Composer<_$KooliDatabase, $KooliNiruvanaTharavugalTableTable> {
  $$KooliNiruvanaTharavugalTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get niruvanathinPeyar => $composableBuilder(
      column: $table.niruvanathinPeyar,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get kurumPeyar => $composableBuilder(
      column: $table.kurumPeyar, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get tholaipaesi1 => $composableBuilder(
      column: $table.tholaipaesi1,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get tholaipaesi2 => $composableBuilder(
      column: $table.tholaipaesi2,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get minnanjal => $composableBuilder(
      column: $table.minnanjal, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get gstin => $composableBuilder(
      column: $table.gstin, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get mugavari => $composableBuilder(
      column: $table.mugavari, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get oor => $composableBuilder(
      column: $table.oor, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get maavattam => $composableBuilder(
      column: $table.maavattam, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get maanilam => $composableBuilder(
      column: $table.maanilam, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get naadu => $composableBuilder(
      column: $table.naadu, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get anjalKuriyeedu => $composableBuilder(
      column: $table.anjalKuriyeedu,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get vangiPeyar => $composableBuilder(
      column: $table.vangiPeyar, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get kilai => $composableBuilder(
      column: $table.kilai, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get vangiKanakku => $composableBuilder(
      column: $table.vangiKanakku,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get ifsc => $composableBuilder(
      column: $table.ifsc, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get oavuru => $composableBuilder(
      column: $table.oavuru, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get agalaOavuru => $composableBuilder(
      column: $table.agalaOavuru, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get thalaippuVadivu => $composableBuilder(
      column: $table.thalaippuVadivu,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get kaiyoppam => $composableBuilder(
      column: $table.kaiyoppam, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get oppamPeyar => $composableBuilder(
      column: $table.oppamPeyar, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get adaimozhi => $composableBuilder(
      column: $table.adaimozhi, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get upiId => $composableBuilder(
      column: $table.upiId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get thoatraNiram => $composableBuilder(
      column: $table.thoatraNiram,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnOrderings(column));
}

class $$KooliNiruvanaTharavugalTableTableAnnotationComposer
    extends Composer<_$KooliDatabase, $KooliNiruvanaTharavugalTableTable> {
  $$KooliNiruvanaTharavugalTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String>
      get niruvanathinPeyar => $composableBuilder(
          column: $table.niruvanathinPeyar, builder: (column) => column);

  GeneratedColumn<String> get kurumPeyar => $composableBuilder(
      column: $table.kurumPeyar, builder: (column) => column);

  GeneratedColumn<String> get tholaipaesi1 => $composableBuilder(
      column: $table.tholaipaesi1, builder: (column) => column);

  GeneratedColumn<String> get tholaipaesi2 => $composableBuilder(
      column: $table.tholaipaesi2, builder: (column) => column);

  GeneratedColumn<String> get minnanjal =>
      $composableBuilder(column: $table.minnanjal, builder: (column) => column);

  GeneratedColumn<String> get gstin =>
      $composableBuilder(column: $table.gstin, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get mugavari =>
      $composableBuilder(column: $table.mugavari, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get oor =>
      $composableBuilder(column: $table.oor, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get maavattam =>
      $composableBuilder(column: $table.maavattam, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get maanilam =>
      $composableBuilder(column: $table.maanilam, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get naadu =>
      $composableBuilder(column: $table.naadu, builder: (column) => column);

  GeneratedColumn<String> get anjalKuriyeedu => $composableBuilder(
      column: $table.anjalKuriyeedu, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String>
      get vangiPeyar => $composableBuilder(
          column: $table.vangiPeyar, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get kilai =>
      $composableBuilder(column: $table.kilai, builder: (column) => column);

  GeneratedColumn<String> get vangiKanakku => $composableBuilder(
      column: $table.vangiKanakku, builder: (column) => column);

  GeneratedColumn<String> get ifsc =>
      $composableBuilder(column: $table.ifsc, builder: (column) => column);

  GeneratedColumn<String> get oavuru =>
      $composableBuilder(column: $table.oavuru, builder: (column) => column);

  GeneratedColumn<String> get agalaOavuru => $composableBuilder(
      column: $table.agalaOavuru, builder: (column) => column);

  GeneratedColumn<String> get thalaippuVadivu => $composableBuilder(
      column: $table.thalaippuVadivu, builder: (column) => column);

  GeneratedColumn<String> get kaiyoppam =>
      $composableBuilder(column: $table.kaiyoppam, builder: (column) => column);

  GeneratedColumn<String> get oppamPeyar => $composableBuilder(
      column: $table.oppamPeyar, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get adaimozhi =>
      $composableBuilder(column: $table.adaimozhi, builder: (column) => column);

  GeneratedColumn<String> get upiId =>
      $composableBuilder(column: $table.upiId, builder: (column) => column);

  GeneratedColumn<String> get thoatraNiram => $composableBuilder(
      column: $table.thoatraNiram, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);

  GeneratedColumn<bool> get isDeleted =>
      $composableBuilder(column: $table.isDeleted, builder: (column) => column);

  GeneratedColumn<DateTime> get deletedAt =>
      $composableBuilder(column: $table.deletedAt, builder: (column) => column);
}

class $$KooliNiruvanaTharavugalTableTableTableManager extends RootTableManager<
    _$KooliDatabase,
    $KooliNiruvanaTharavugalTableTable,
    KooliNiruvanaTharavugalEntry,
    $$KooliNiruvanaTharavugalTableTableFilterComposer,
    $$KooliNiruvanaTharavugalTableTableOrderingComposer,
    $$KooliNiruvanaTharavugalTableTableAnnotationComposer,
    $$KooliNiruvanaTharavugalTableTableCreateCompanionBuilder,
    $$KooliNiruvanaTharavugalTableTableUpdateCompanionBuilder,
    (
      KooliNiruvanaTharavugalEntry,
      BaseReferences<_$KooliDatabase, $KooliNiruvanaTharavugalTableTable,
          KooliNiruvanaTharavugalEntry>
    ),
    KooliNiruvanaTharavugalEntry,
    PrefetchHooks Function()> {
  $$KooliNiruvanaTharavugalTableTableTableManager(
      _$KooliDatabase db, $KooliNiruvanaTharavugalTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$KooliNiruvanaTharavugalTableTableFilterComposer(
                  $db: db, $table: table),
          createOrderingComposer: () =>
              $$KooliNiruvanaTharavugalTableTableOrderingComposer(
                  $db: db, $table: table),
          createComputedFieldComposer: () =>
              $$KooliNiruvanaTharavugalTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<Map<String, String>> niruvanathinPeyar = const Value.absent(),
            Value<String> kurumPeyar = const Value.absent(),
            Value<String> tholaipaesi1 = const Value.absent(),
            Value<String> tholaipaesi2 = const Value.absent(),
            Value<String> minnanjal = const Value.absent(),
            Value<String> gstin = const Value.absent(),
            Value<Map<String, String>> mugavari = const Value.absent(),
            Value<Map<String, String>> oor = const Value.absent(),
            Value<Map<String, String>> maavattam = const Value.absent(),
            Value<Map<String, String>> maanilam = const Value.absent(),
            Value<Map<String, String>> naadu = const Value.absent(),
            Value<String> anjalKuriyeedu = const Value.absent(),
            Value<Map<String, String>> vangiPeyar = const Value.absent(),
            Value<Map<String, String>> kilai = const Value.absent(),
            Value<String> vangiKanakku = const Value.absent(),
            Value<String> ifsc = const Value.absent(),
            Value<String> oavuru = const Value.absent(),
            Value<String> agalaOavuru = const Value.absent(),
            Value<String> thalaippuVadivu = const Value.absent(),
            Value<String> kaiyoppam = const Value.absent(),
            Value<String> oppamPeyar = const Value.absent(),
            Value<Map<String, String>> adaimozhi = const Value.absent(),
            Value<String> upiId = const Value.absent(),
            Value<String> thoatraNiram = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliNiruvanaTharavugalTableCompanion(
            id: id,
            niruvanathinPeyar: niruvanathinPeyar,
            kurumPeyar: kurumPeyar,
            tholaipaesi1: tholaipaesi1,
            tholaipaesi2: tholaipaesi2,
            minnanjal: minnanjal,
            gstin: gstin,
            mugavari: mugavari,
            oor: oor,
            maavattam: maavattam,
            maanilam: maanilam,
            naadu: naadu,
            anjalKuriyeedu: anjalKuriyeedu,
            vangiPeyar: vangiPeyar,
            kilai: kilai,
            vangiKanakku: vangiKanakku,
            ifsc: ifsc,
            oavuru: oavuru,
            agalaOavuru: agalaOavuru,
            thalaippuVadivu: thalaippuVadivu,
            kaiyoppam: kaiyoppam,
            oppamPeyar: oppamPeyar,
            adaimozhi: adaimozhi,
            upiId: upiId,
            thoatraNiram: thoatraNiram,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<Map<String, String>> niruvanathinPeyar = const Value.absent(),
            Value<String> kurumPeyar = const Value.absent(),
            Value<String> tholaipaesi1 = const Value.absent(),
            Value<String> tholaipaesi2 = const Value.absent(),
            Value<String> minnanjal = const Value.absent(),
            Value<String> gstin = const Value.absent(),
            Value<Map<String, String>> mugavari = const Value.absent(),
            Value<Map<String, String>> oor = const Value.absent(),
            Value<Map<String, String>> maavattam = const Value.absent(),
            Value<Map<String, String>> maanilam = const Value.absent(),
            Value<Map<String, String>> naadu = const Value.absent(),
            Value<String> anjalKuriyeedu = const Value.absent(),
            Value<Map<String, String>> vangiPeyar = const Value.absent(),
            Value<Map<String, String>> kilai = const Value.absent(),
            Value<String> vangiKanakku = const Value.absent(),
            Value<String> ifsc = const Value.absent(),
            Value<String> oavuru = const Value.absent(),
            Value<String> agalaOavuru = const Value.absent(),
            Value<String> thalaippuVadivu = const Value.absent(),
            Value<String> kaiyoppam = const Value.absent(),
            Value<String> oppamPeyar = const Value.absent(),
            Value<Map<String, String>> adaimozhi = const Value.absent(),
            Value<String> upiId = const Value.absent(),
            Value<String> thoatraNiram = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliNiruvanaTharavugalTableCompanion.insert(
            id: id,
            niruvanathinPeyar: niruvanathinPeyar,
            kurumPeyar: kurumPeyar,
            tholaipaesi1: tholaipaesi1,
            tholaipaesi2: tholaipaesi2,
            minnanjal: minnanjal,
            gstin: gstin,
            mugavari: mugavari,
            oor: oor,
            maavattam: maavattam,
            maanilam: maanilam,
            naadu: naadu,
            anjalKuriyeedu: anjalKuriyeedu,
            vangiPeyar: vangiPeyar,
            kilai: kilai,
            vangiKanakku: vangiKanakku,
            ifsc: ifsc,
            oavuru: oavuru,
            agalaOavuru: agalaOavuru,
            thalaippuVadivu: thalaippuVadivu,
            kaiyoppam: kaiyoppam,
            oppamPeyar: oppamPeyar,
            adaimozhi: adaimozhi,
            upiId: upiId,
            thoatraNiram: thoatraNiram,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$KooliNiruvanaTharavugalTableTableProcessedTableManager
    = ProcessedTableManager<
        _$KooliDatabase,
        $KooliNiruvanaTharavugalTableTable,
        KooliNiruvanaTharavugalEntry,
        $$KooliNiruvanaTharavugalTableTableFilterComposer,
        $$KooliNiruvanaTharavugalTableTableOrderingComposer,
        $$KooliNiruvanaTharavugalTableTableAnnotationComposer,
        $$KooliNiruvanaTharavugalTableTableCreateCompanionBuilder,
        $$KooliNiruvanaTharavugalTableTableUpdateCompanionBuilder,
        (
          KooliNiruvanaTharavugalEntry,
          BaseReferences<_$KooliDatabase, $KooliNiruvanaTharavugalTableTable,
              KooliNiruvanaTharavugalEntry>
        ),
        KooliNiruvanaTharavugalEntry,
        PrefetchHooks Function()>;
typedef $$KooliVaangunarTableTableCreateCompanionBuilder
    = KooliVaangunarTableCompanion Function({
  Value<int> id,
  Value<Map<String, String>> peyar,
  Value<Map<String, String>> mugavari,
  Value<Map<String, String>> oor,
  Value<Map<String, String>> maavattam,
  Value<Map<String, String>> maanilam,
  Value<Map<String, String>> naadu,
  Value<Map<String, String>> velinaadMugavari,
  Value<String> anjalKuriyeedu,
  Value<String> gstin,
  Value<String> minnanjal,
  Value<String> tholaipaesi,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});
typedef $$KooliVaangunarTableTableUpdateCompanionBuilder
    = KooliVaangunarTableCompanion Function({
  Value<int> id,
  Value<Map<String, String>> peyar,
  Value<Map<String, String>> mugavari,
  Value<Map<String, String>> oor,
  Value<Map<String, String>> maavattam,
  Value<Map<String, String>> maanilam,
  Value<Map<String, String>> naadu,
  Value<Map<String, String>> velinaadMugavari,
  Value<String> anjalKuriyeedu,
  Value<String> gstin,
  Value<String> minnanjal,
  Value<String> tholaipaesi,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});

class $$KooliVaangunarTableTableFilterComposer
    extends Composer<_$KooliDatabase, $KooliVaangunarTableTable> {
  $$KooliVaangunarTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get peyar => $composableBuilder(
          column: $table.peyar,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get mugavari => $composableBuilder(
          column: $table.mugavari,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get oor => $composableBuilder(
          column: $table.oor,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get maavattam => $composableBuilder(
          column: $table.maavattam,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get maanilam => $composableBuilder(
          column: $table.maanilam,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get naadu => $composableBuilder(
          column: $table.naadu,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get velinaadMugavari => $composableBuilder(
          column: $table.velinaadMugavari,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnFilters<String> get anjalKuriyeedu => $composableBuilder(
      column: $table.anjalKuriyeedu,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get gstin => $composableBuilder(
      column: $table.gstin, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get minnanjal => $composableBuilder(
      column: $table.minnanjal, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get tholaipaesi => $composableBuilder(
      column: $table.tholaipaesi, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnFilters(column));
}

class $$KooliVaangunarTableTableOrderingComposer
    extends Composer<_$KooliDatabase, $KooliVaangunarTableTable> {
  $$KooliVaangunarTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get peyar => $composableBuilder(
      column: $table.peyar, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get mugavari => $composableBuilder(
      column: $table.mugavari, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get oor => $composableBuilder(
      column: $table.oor, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get maavattam => $composableBuilder(
      column: $table.maavattam, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get maanilam => $composableBuilder(
      column: $table.maanilam, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get naadu => $composableBuilder(
      column: $table.naadu, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get velinaadMugavari => $composableBuilder(
      column: $table.velinaadMugavari,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get anjalKuriyeedu => $composableBuilder(
      column: $table.anjalKuriyeedu,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get gstin => $composableBuilder(
      column: $table.gstin, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get minnanjal => $composableBuilder(
      column: $table.minnanjal, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get tholaipaesi => $composableBuilder(
      column: $table.tholaipaesi, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnOrderings(column));
}

class $$KooliVaangunarTableTableAnnotationComposer
    extends Composer<_$KooliDatabase, $KooliVaangunarTableTable> {
  $$KooliVaangunarTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get peyar =>
      $composableBuilder(column: $table.peyar, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get mugavari =>
      $composableBuilder(column: $table.mugavari, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get oor =>
      $composableBuilder(column: $table.oor, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get maavattam =>
      $composableBuilder(column: $table.maavattam, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get maanilam =>
      $composableBuilder(column: $table.maanilam, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String> get naadu =>
      $composableBuilder(column: $table.naadu, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String>
      get velinaadMugavari => $composableBuilder(
          column: $table.velinaadMugavari, builder: (column) => column);

  GeneratedColumn<String> get anjalKuriyeedu => $composableBuilder(
      column: $table.anjalKuriyeedu, builder: (column) => column);

  GeneratedColumn<String> get gstin =>
      $composableBuilder(column: $table.gstin, builder: (column) => column);

  GeneratedColumn<String> get minnanjal =>
      $composableBuilder(column: $table.minnanjal, builder: (column) => column);

  GeneratedColumn<String> get tholaipaesi => $composableBuilder(
      column: $table.tholaipaesi, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);

  GeneratedColumn<bool> get isDeleted =>
      $composableBuilder(column: $table.isDeleted, builder: (column) => column);

  GeneratedColumn<DateTime> get deletedAt =>
      $composableBuilder(column: $table.deletedAt, builder: (column) => column);
}

class $$KooliVaangunarTableTableTableManager extends RootTableManager<
    _$KooliDatabase,
    $KooliVaangunarTableTable,
    KooliVaangunarEntry,
    $$KooliVaangunarTableTableFilterComposer,
    $$KooliVaangunarTableTableOrderingComposer,
    $$KooliVaangunarTableTableAnnotationComposer,
    $$KooliVaangunarTableTableCreateCompanionBuilder,
    $$KooliVaangunarTableTableUpdateCompanionBuilder,
    (
      KooliVaangunarEntry,
      BaseReferences<_$KooliDatabase, $KooliVaangunarTableTable,
          KooliVaangunarEntry>
    ),
    KooliVaangunarEntry,
    PrefetchHooks Function()> {
  $$KooliVaangunarTableTableTableManager(
      _$KooliDatabase db, $KooliVaangunarTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$KooliVaangunarTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$KooliVaangunarTableTableOrderingComposer(
                  $db: db, $table: table),
          createComputedFieldComposer: () =>
              $$KooliVaangunarTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<Map<String, String>> peyar = const Value.absent(),
            Value<Map<String, String>> mugavari = const Value.absent(),
            Value<Map<String, String>> oor = const Value.absent(),
            Value<Map<String, String>> maavattam = const Value.absent(),
            Value<Map<String, String>> maanilam = const Value.absent(),
            Value<Map<String, String>> naadu = const Value.absent(),
            Value<Map<String, String>> velinaadMugavari = const Value.absent(),
            Value<String> anjalKuriyeedu = const Value.absent(),
            Value<String> gstin = const Value.absent(),
            Value<String> minnanjal = const Value.absent(),
            Value<String> tholaipaesi = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliVaangunarTableCompanion(
            id: id,
            peyar: peyar,
            mugavari: mugavari,
            oor: oor,
            maavattam: maavattam,
            maanilam: maanilam,
            naadu: naadu,
            velinaadMugavari: velinaadMugavari,
            anjalKuriyeedu: anjalKuriyeedu,
            gstin: gstin,
            minnanjal: minnanjal,
            tholaipaesi: tholaipaesi,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<Map<String, String>> peyar = const Value.absent(),
            Value<Map<String, String>> mugavari = const Value.absent(),
            Value<Map<String, String>> oor = const Value.absent(),
            Value<Map<String, String>> maavattam = const Value.absent(),
            Value<Map<String, String>> maanilam = const Value.absent(),
            Value<Map<String, String>> naadu = const Value.absent(),
            Value<Map<String, String>> velinaadMugavari = const Value.absent(),
            Value<String> anjalKuriyeedu = const Value.absent(),
            Value<String> gstin = const Value.absent(),
            Value<String> minnanjal = const Value.absent(),
            Value<String> tholaipaesi = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliVaangunarTableCompanion.insert(
            id: id,
            peyar: peyar,
            mugavari: mugavari,
            oor: oor,
            maavattam: maavattam,
            maanilam: maanilam,
            naadu: naadu,
            velinaadMugavari: velinaadMugavari,
            anjalKuriyeedu: anjalKuriyeedu,
            gstin: gstin,
            minnanjal: minnanjal,
            tholaipaesi: tholaipaesi,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$KooliVaangunarTableTableProcessedTableManager = ProcessedTableManager<
    _$KooliDatabase,
    $KooliVaangunarTableTable,
    KooliVaangunarEntry,
    $$KooliVaangunarTableTableFilterComposer,
    $$KooliVaangunarTableTableOrderingComposer,
    $$KooliVaangunarTableTableAnnotationComposer,
    $$KooliVaangunarTableTableCreateCompanionBuilder,
    $$KooliVaangunarTableTableUpdateCompanionBuilder,
    (
      KooliVaangunarEntry,
      BaseReferences<_$KooliDatabase, $KooliVaangunarTableTable,
          KooliVaangunarEntry>
    ),
    KooliVaangunarEntry,
    PrefetchHooks Function()>;
typedef $$KooliPorulTableTableCreateCompanionBuilder = KooliPorulTableCompanion
    Function({
  Value<int> id,
  Value<Map<String, String>> porulPeyar,
  Value<String> hsnCode,
  Value<double> vilai,
  Value<double> variVeetham,
  Value<String> alavuVagai,
  Value<String> alagu,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});
typedef $$KooliPorulTableTableUpdateCompanionBuilder = KooliPorulTableCompanion
    Function({
  Value<int> id,
  Value<Map<String, String>> porulPeyar,
  Value<String> hsnCode,
  Value<double> vilai,
  Value<double> variVeetham,
  Value<String> alavuVagai,
  Value<String> alagu,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});

class $$KooliPorulTableTableFilterComposer
    extends Composer<_$KooliDatabase, $KooliPorulTableTable> {
  $$KooliPorulTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get porulPeyar => $composableBuilder(
          column: $table.porulPeyar,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnFilters<String> get hsnCode => $composableBuilder(
      column: $table.hsnCode, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get vilai => $composableBuilder(
      column: $table.vilai, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get variVeetham => $composableBuilder(
      column: $table.variVeetham, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get alavuVagai => $composableBuilder(
      column: $table.alavuVagai, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get alagu => $composableBuilder(
      column: $table.alagu, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnFilters(column));
}

class $$KooliPorulTableTableOrderingComposer
    extends Composer<_$KooliDatabase, $KooliPorulTableTable> {
  $$KooliPorulTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get porulPeyar => $composableBuilder(
      column: $table.porulPeyar, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get hsnCode => $composableBuilder(
      column: $table.hsnCode, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get vilai => $composableBuilder(
      column: $table.vilai, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get variVeetham => $composableBuilder(
      column: $table.variVeetham, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get alavuVagai => $composableBuilder(
      column: $table.alavuVagai, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get alagu => $composableBuilder(
      column: $table.alagu, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnOrderings(column));
}

class $$KooliPorulTableTableAnnotationComposer
    extends Composer<_$KooliDatabase, $KooliPorulTableTable> {
  $$KooliPorulTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String>
      get porulPeyar => $composableBuilder(
          column: $table.porulPeyar, builder: (column) => column);

  GeneratedColumn<String> get hsnCode =>
      $composableBuilder(column: $table.hsnCode, builder: (column) => column);

  GeneratedColumn<double> get vilai =>
      $composableBuilder(column: $table.vilai, builder: (column) => column);

  GeneratedColumn<double> get variVeetham => $composableBuilder(
      column: $table.variVeetham, builder: (column) => column);

  GeneratedColumn<String> get alavuVagai => $composableBuilder(
      column: $table.alavuVagai, builder: (column) => column);

  GeneratedColumn<String> get alagu =>
      $composableBuilder(column: $table.alagu, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);

  GeneratedColumn<bool> get isDeleted =>
      $composableBuilder(column: $table.isDeleted, builder: (column) => column);

  GeneratedColumn<DateTime> get deletedAt =>
      $composableBuilder(column: $table.deletedAt, builder: (column) => column);
}

class $$KooliPorulTableTableTableManager extends RootTableManager<
    _$KooliDatabase,
    $KooliPorulTableTable,
    KooliPorulEntry,
    $$KooliPorulTableTableFilterComposer,
    $$KooliPorulTableTableOrderingComposer,
    $$KooliPorulTableTableAnnotationComposer,
    $$KooliPorulTableTableCreateCompanionBuilder,
    $$KooliPorulTableTableUpdateCompanionBuilder,
    (
      KooliPorulEntry,
      BaseReferences<_$KooliDatabase, $KooliPorulTableTable, KooliPorulEntry>
    ),
    KooliPorulEntry,
    PrefetchHooks Function()> {
  $$KooliPorulTableTableTableManager(
      _$KooliDatabase db, $KooliPorulTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$KooliPorulTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$KooliPorulTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$KooliPorulTableTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<Map<String, String>> porulPeyar = const Value.absent(),
            Value<String> hsnCode = const Value.absent(),
            Value<double> vilai = const Value.absent(),
            Value<double> variVeetham = const Value.absent(),
            Value<String> alavuVagai = const Value.absent(),
            Value<String> alagu = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliPorulTableCompanion(
            id: id,
            porulPeyar: porulPeyar,
            hsnCode: hsnCode,
            vilai: vilai,
            variVeetham: variVeetham,
            alavuVagai: alavuVagai,
            alagu: alagu,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<Map<String, String>> porulPeyar = const Value.absent(),
            Value<String> hsnCode = const Value.absent(),
            Value<double> vilai = const Value.absent(),
            Value<double> variVeetham = const Value.absent(),
            Value<String> alavuVagai = const Value.absent(),
            Value<String> alagu = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliPorulTableCompanion.insert(
            id: id,
            porulPeyar: porulPeyar,
            hsnCode: hsnCode,
            vilai: vilai,
            variVeetham: variVeetham,
            alavuVagai: alavuVagai,
            alagu: alagu,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$KooliPorulTableTableProcessedTableManager = ProcessedTableManager<
    _$KooliDatabase,
    $KooliPorulTableTable,
    KooliPorulEntry,
    $$KooliPorulTableTableFilterComposer,
    $$KooliPorulTableTableOrderingComposer,
    $$KooliPorulTableTableAnnotationComposer,
    $$KooliPorulTableTableCreateCompanionBuilder,
    $$KooliPorulTableTableUpdateCompanionBuilder,
    (
      KooliPorulEntry,
      BaseReferences<_$KooliDatabase, $KooliPorulTableTable, KooliPorulEntry>
    ),
    KooliPorulEntry,
    PrefetchHooks Function()>;
typedef $$KooliPattiyalTableTableCreateCompanionBuilder
    = KooliPattiyalTableCompanion Function({
  Value<int> id,
  Value<int?> niruvanamId,
  required String patrucheettuEn,
  required int finYear,
  Value<int> vanakkam,
  Value<String> pattiyalVagai,
  Value<int?> vaangunarId,
  Value<Map<String, String>> vaangunarPeyar,
  Value<Map<String, String>> vaangunarMunvari,
  Value<DateTime> pattiyalNaal,
  Value<String> tharavugal,
  Value<double> mothaThogai,
  Value<double> thallupadi,
  Value<double> podhuThallupadiMathippu,
  Value<String> podhuThallupadiVagai,
  Value<double> podhuThallupadiThogai,
  Value<double> variThogai,
  Value<String> variTharavugal,
  Value<double> mothaEdai,
  Value<double> setharamGrams,
  Value<double> thabaalThogai,
  Value<double> ahimsaPattuThogai,
  Value<String> piravariVugal,
  Value<String> sonthaViruppangal,
  Value<String> nibandhanaigal,
  Value<String> ullkurippu,
  Value<String> vangiTharavugal,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});
typedef $$KooliPattiyalTableTableUpdateCompanionBuilder
    = KooliPattiyalTableCompanion Function({
  Value<int> id,
  Value<int?> niruvanamId,
  Value<String> patrucheettuEn,
  Value<int> finYear,
  Value<int> vanakkam,
  Value<String> pattiyalVagai,
  Value<int?> vaangunarId,
  Value<Map<String, String>> vaangunarPeyar,
  Value<Map<String, String>> vaangunarMunvari,
  Value<DateTime> pattiyalNaal,
  Value<String> tharavugal,
  Value<double> mothaThogai,
  Value<double> thallupadi,
  Value<double> podhuThallupadiMathippu,
  Value<String> podhuThallupadiVagai,
  Value<double> podhuThallupadiThogai,
  Value<double> variThogai,
  Value<String> variTharavugal,
  Value<double> mothaEdai,
  Value<double> setharamGrams,
  Value<double> thabaalThogai,
  Value<double> ahimsaPattuThogai,
  Value<String> piravariVugal,
  Value<String> sonthaViruppangal,
  Value<String> nibandhanaigal,
  Value<String> ullkurippu,
  Value<String> vangiTharavugal,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});

class $$KooliPattiyalTableTableFilterComposer
    extends Composer<_$KooliDatabase, $KooliPattiyalTableTable> {
  $$KooliPattiyalTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get niruvanamId => $composableBuilder(
      column: $table.niruvanamId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get patrucheettuEn => $composableBuilder(
      column: $table.patrucheettuEn,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get finYear => $composableBuilder(
      column: $table.finYear, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get vanakkam => $composableBuilder(
      column: $table.vanakkam, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get pattiyalVagai => $composableBuilder(
      column: $table.pattiyalVagai, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get vaangunarId => $composableBuilder(
      column: $table.vaangunarId, builder: (column) => ColumnFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get vaangunarPeyar => $composableBuilder(
          column: $table.vaangunarPeyar,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get vaangunarMunvari => $composableBuilder(
          column: $table.vaangunarMunvari,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnFilters<DateTime> get pattiyalNaal => $composableBuilder(
      column: $table.pattiyalNaal, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get tharavugal => $composableBuilder(
      column: $table.tharavugal, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get mothaThogai => $composableBuilder(
      column: $table.mothaThogai, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get thallupadi => $composableBuilder(
      column: $table.thallupadi, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get podhuThallupadiMathippu => $composableBuilder(
      column: $table.podhuThallupadiMathippu,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get podhuThallupadiVagai => $composableBuilder(
      column: $table.podhuThallupadiVagai,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get podhuThallupadiThogai => $composableBuilder(
      column: $table.podhuThallupadiThogai,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get variThogai => $composableBuilder(
      column: $table.variThogai, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get variTharavugal => $composableBuilder(
      column: $table.variTharavugal,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get mothaEdai => $composableBuilder(
      column: $table.mothaEdai, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get setharamGrams => $composableBuilder(
      column: $table.setharamGrams, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get thabaalThogai => $composableBuilder(
      column: $table.thabaalThogai, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get ahimsaPattuThogai => $composableBuilder(
      column: $table.ahimsaPattuThogai,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get piravariVugal => $composableBuilder(
      column: $table.piravariVugal, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get sonthaViruppangal => $composableBuilder(
      column: $table.sonthaViruppangal,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get nibandhanaigal => $composableBuilder(
      column: $table.nibandhanaigal,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get ullkurippu => $composableBuilder(
      column: $table.ullkurippu, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get vangiTharavugal => $composableBuilder(
      column: $table.vangiTharavugal,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnFilters(column));
}

class $$KooliPattiyalTableTableOrderingComposer
    extends Composer<_$KooliDatabase, $KooliPattiyalTableTable> {
  $$KooliPattiyalTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get niruvanamId => $composableBuilder(
      column: $table.niruvanamId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get patrucheettuEn => $composableBuilder(
      column: $table.patrucheettuEn,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get finYear => $composableBuilder(
      column: $table.finYear, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get vanakkam => $composableBuilder(
      column: $table.vanakkam, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get pattiyalVagai => $composableBuilder(
      column: $table.pattiyalVagai,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get vaangunarId => $composableBuilder(
      column: $table.vaangunarId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get vaangunarPeyar => $composableBuilder(
      column: $table.vaangunarPeyar,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get vaangunarMunvari => $composableBuilder(
      column: $table.vaangunarMunvari,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get pattiyalNaal => $composableBuilder(
      column: $table.pattiyalNaal,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get tharavugal => $composableBuilder(
      column: $table.tharavugal, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get mothaThogai => $composableBuilder(
      column: $table.mothaThogai, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get thallupadi => $composableBuilder(
      column: $table.thallupadi, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get podhuThallupadiMathippu => $composableBuilder(
      column: $table.podhuThallupadiMathippu,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get podhuThallupadiVagai => $composableBuilder(
      column: $table.podhuThallupadiVagai,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get podhuThallupadiThogai => $composableBuilder(
      column: $table.podhuThallupadiThogai,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get variThogai => $composableBuilder(
      column: $table.variThogai, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get variTharavugal => $composableBuilder(
      column: $table.variTharavugal,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get mothaEdai => $composableBuilder(
      column: $table.mothaEdai, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get setharamGrams => $composableBuilder(
      column: $table.setharamGrams,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get thabaalThogai => $composableBuilder(
      column: $table.thabaalThogai,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get ahimsaPattuThogai => $composableBuilder(
      column: $table.ahimsaPattuThogai,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get piravariVugal => $composableBuilder(
      column: $table.piravariVugal,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get sonthaViruppangal => $composableBuilder(
      column: $table.sonthaViruppangal,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get nibandhanaigal => $composableBuilder(
      column: $table.nibandhanaigal,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get ullkurippu => $composableBuilder(
      column: $table.ullkurippu, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get vangiTharavugal => $composableBuilder(
      column: $table.vangiTharavugal,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnOrderings(column));
}

class $$KooliPattiyalTableTableAnnotationComposer
    extends Composer<_$KooliDatabase, $KooliPattiyalTableTable> {
  $$KooliPattiyalTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<int> get niruvanamId => $composableBuilder(
      column: $table.niruvanamId, builder: (column) => column);

  GeneratedColumn<String> get patrucheettuEn => $composableBuilder(
      column: $table.patrucheettuEn, builder: (column) => column);

  GeneratedColumn<int> get finYear =>
      $composableBuilder(column: $table.finYear, builder: (column) => column);

  GeneratedColumn<int> get vanakkam =>
      $composableBuilder(column: $table.vanakkam, builder: (column) => column);

  GeneratedColumn<String> get pattiyalVagai => $composableBuilder(
      column: $table.pattiyalVagai, builder: (column) => column);

  GeneratedColumn<int> get vaangunarId => $composableBuilder(
      column: $table.vaangunarId, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String>
      get vaangunarPeyar => $composableBuilder(
          column: $table.vaangunarPeyar, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String>
      get vaangunarMunvari => $composableBuilder(
          column: $table.vaangunarMunvari, builder: (column) => column);

  GeneratedColumn<DateTime> get pattiyalNaal => $composableBuilder(
      column: $table.pattiyalNaal, builder: (column) => column);

  GeneratedColumn<String> get tharavugal => $composableBuilder(
      column: $table.tharavugal, builder: (column) => column);

  GeneratedColumn<double> get mothaThogai => $composableBuilder(
      column: $table.mothaThogai, builder: (column) => column);

  GeneratedColumn<double> get thallupadi => $composableBuilder(
      column: $table.thallupadi, builder: (column) => column);

  GeneratedColumn<double> get podhuThallupadiMathippu => $composableBuilder(
      column: $table.podhuThallupadiMathippu, builder: (column) => column);

  GeneratedColumn<String> get podhuThallupadiVagai => $composableBuilder(
      column: $table.podhuThallupadiVagai, builder: (column) => column);

  GeneratedColumn<double> get podhuThallupadiThogai => $composableBuilder(
      column: $table.podhuThallupadiThogai, builder: (column) => column);

  GeneratedColumn<double> get variThogai => $composableBuilder(
      column: $table.variThogai, builder: (column) => column);

  GeneratedColumn<String> get variTharavugal => $composableBuilder(
      column: $table.variTharavugal, builder: (column) => column);

  GeneratedColumn<double> get mothaEdai =>
      $composableBuilder(column: $table.mothaEdai, builder: (column) => column);

  GeneratedColumn<double> get setharamGrams => $composableBuilder(
      column: $table.setharamGrams, builder: (column) => column);

  GeneratedColumn<double> get thabaalThogai => $composableBuilder(
      column: $table.thabaalThogai, builder: (column) => column);

  GeneratedColumn<double> get ahimsaPattuThogai => $composableBuilder(
      column: $table.ahimsaPattuThogai, builder: (column) => column);

  GeneratedColumn<String> get piravariVugal => $composableBuilder(
      column: $table.piravariVugal, builder: (column) => column);

  GeneratedColumn<String> get sonthaViruppangal => $composableBuilder(
      column: $table.sonthaViruppangal, builder: (column) => column);

  GeneratedColumn<String> get nibandhanaigal => $composableBuilder(
      column: $table.nibandhanaigal, builder: (column) => column);

  GeneratedColumn<String> get ullkurippu => $composableBuilder(
      column: $table.ullkurippu, builder: (column) => column);

  GeneratedColumn<String> get vangiTharavugal => $composableBuilder(
      column: $table.vangiTharavugal, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);

  GeneratedColumn<bool> get isDeleted =>
      $composableBuilder(column: $table.isDeleted, builder: (column) => column);

  GeneratedColumn<DateTime> get deletedAt =>
      $composableBuilder(column: $table.deletedAt, builder: (column) => column);
}

class $$KooliPattiyalTableTableTableManager extends RootTableManager<
    _$KooliDatabase,
    $KooliPattiyalTableTable,
    KooliPattiyalEntry,
    $$KooliPattiyalTableTableFilterComposer,
    $$KooliPattiyalTableTableOrderingComposer,
    $$KooliPattiyalTableTableAnnotationComposer,
    $$KooliPattiyalTableTableCreateCompanionBuilder,
    $$KooliPattiyalTableTableUpdateCompanionBuilder,
    (
      KooliPattiyalEntry,
      BaseReferences<_$KooliDatabase, $KooliPattiyalTableTable,
          KooliPattiyalEntry>
    ),
    KooliPattiyalEntry,
    PrefetchHooks Function()> {
  $$KooliPattiyalTableTableTableManager(
      _$KooliDatabase db, $KooliPattiyalTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$KooliPattiyalTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$KooliPattiyalTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$KooliPattiyalTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<int?> niruvanamId = const Value.absent(),
            Value<String> patrucheettuEn = const Value.absent(),
            Value<int> finYear = const Value.absent(),
            Value<int> vanakkam = const Value.absent(),
            Value<String> pattiyalVagai = const Value.absent(),
            Value<int?> vaangunarId = const Value.absent(),
            Value<Map<String, String>> vaangunarPeyar = const Value.absent(),
            Value<Map<String, String>> vaangunarMunvari = const Value.absent(),
            Value<DateTime> pattiyalNaal = const Value.absent(),
            Value<String> tharavugal = const Value.absent(),
            Value<double> mothaThogai = const Value.absent(),
            Value<double> thallupadi = const Value.absent(),
            Value<double> podhuThallupadiMathippu = const Value.absent(),
            Value<String> podhuThallupadiVagai = const Value.absent(),
            Value<double> podhuThallupadiThogai = const Value.absent(),
            Value<double> variThogai = const Value.absent(),
            Value<String> variTharavugal = const Value.absent(),
            Value<double> mothaEdai = const Value.absent(),
            Value<double> setharamGrams = const Value.absent(),
            Value<double> thabaalThogai = const Value.absent(),
            Value<double> ahimsaPattuThogai = const Value.absent(),
            Value<String> piravariVugal = const Value.absent(),
            Value<String> sonthaViruppangal = const Value.absent(),
            Value<String> nibandhanaigal = const Value.absent(),
            Value<String> ullkurippu = const Value.absent(),
            Value<String> vangiTharavugal = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliPattiyalTableCompanion(
            id: id,
            niruvanamId: niruvanamId,
            patrucheettuEn: patrucheettuEn,
            finYear: finYear,
            vanakkam: vanakkam,
            pattiyalVagai: pattiyalVagai,
            vaangunarId: vaangunarId,
            vaangunarPeyar: vaangunarPeyar,
            vaangunarMunvari: vaangunarMunvari,
            pattiyalNaal: pattiyalNaal,
            tharavugal: tharavugal,
            mothaThogai: mothaThogai,
            thallupadi: thallupadi,
            podhuThallupadiMathippu: podhuThallupadiMathippu,
            podhuThallupadiVagai: podhuThallupadiVagai,
            podhuThallupadiThogai: podhuThallupadiThogai,
            variThogai: variThogai,
            variTharavugal: variTharavugal,
            mothaEdai: mothaEdai,
            setharamGrams: setharamGrams,
            thabaalThogai: thabaalThogai,
            ahimsaPattuThogai: ahimsaPattuThogai,
            piravariVugal: piravariVugal,
            sonthaViruppangal: sonthaViruppangal,
            nibandhanaigal: nibandhanaigal,
            ullkurippu: ullkurippu,
            vangiTharavugal: vangiTharavugal,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<int?> niruvanamId = const Value.absent(),
            required String patrucheettuEn,
            required int finYear,
            Value<int> vanakkam = const Value.absent(),
            Value<String> pattiyalVagai = const Value.absent(),
            Value<int?> vaangunarId = const Value.absent(),
            Value<Map<String, String>> vaangunarPeyar = const Value.absent(),
            Value<Map<String, String>> vaangunarMunvari = const Value.absent(),
            Value<DateTime> pattiyalNaal = const Value.absent(),
            Value<String> tharavugal = const Value.absent(),
            Value<double> mothaThogai = const Value.absent(),
            Value<double> thallupadi = const Value.absent(),
            Value<double> podhuThallupadiMathippu = const Value.absent(),
            Value<String> podhuThallupadiVagai = const Value.absent(),
            Value<double> podhuThallupadiThogai = const Value.absent(),
            Value<double> variThogai = const Value.absent(),
            Value<String> variTharavugal = const Value.absent(),
            Value<double> mothaEdai = const Value.absent(),
            Value<double> setharamGrams = const Value.absent(),
            Value<double> thabaalThogai = const Value.absent(),
            Value<double> ahimsaPattuThogai = const Value.absent(),
            Value<String> piravariVugal = const Value.absent(),
            Value<String> sonthaViruppangal = const Value.absent(),
            Value<String> nibandhanaigal = const Value.absent(),
            Value<String> ullkurippu = const Value.absent(),
            Value<String> vangiTharavugal = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliPattiyalTableCompanion.insert(
            id: id,
            niruvanamId: niruvanamId,
            patrucheettuEn: patrucheettuEn,
            finYear: finYear,
            vanakkam: vanakkam,
            pattiyalVagai: pattiyalVagai,
            vaangunarId: vaangunarId,
            vaangunarPeyar: vaangunarPeyar,
            vaangunarMunvari: vaangunarMunvari,
            pattiyalNaal: pattiyalNaal,
            tharavugal: tharavugal,
            mothaThogai: mothaThogai,
            thallupadi: thallupadi,
            podhuThallupadiMathippu: podhuThallupadiMathippu,
            podhuThallupadiVagai: podhuThallupadiVagai,
            podhuThallupadiThogai: podhuThallupadiThogai,
            variThogai: variThogai,
            variTharavugal: variTharavugal,
            mothaEdai: mothaEdai,
            setharamGrams: setharamGrams,
            thabaalThogai: thabaalThogai,
            ahimsaPattuThogai: ahimsaPattuThogai,
            piravariVugal: piravariVugal,
            sonthaViruppangal: sonthaViruppangal,
            nibandhanaigal: nibandhanaigal,
            ullkurippu: ullkurippu,
            vangiTharavugal: vangiTharavugal,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$KooliPattiyalTableTableProcessedTableManager = ProcessedTableManager<
    _$KooliDatabase,
    $KooliPattiyalTableTable,
    KooliPattiyalEntry,
    $$KooliPattiyalTableTableFilterComposer,
    $$KooliPattiyalTableTableOrderingComposer,
    $$KooliPattiyalTableTableAnnotationComposer,
    $$KooliPattiyalTableTableCreateCompanionBuilder,
    $$KooliPattiyalTableTableUpdateCompanionBuilder,
    (
      KooliPattiyalEntry,
      BaseReferences<_$KooliDatabase, $KooliPattiyalTableTable,
          KooliPattiyalEntry>
    ),
    KooliPattiyalEntry,
    PrefetchHooks Function()>;
typedef $$KooliPatrugalTableTableCreateCompanionBuilder
    = KooliPatrugalTableCompanion Function({
  Value<int> id,
  Value<int?> niruvanamId,
  required String patruEn,
  Value<String> finYear,
  Value<int> vanakkam,
  Value<int?> vaangunarId,
  Value<Map<String, String>> vaangunarPeyar,
  Value<Map<String, String>> vaangunarMunvari,
  Value<DateTime> patruNaal,
  Value<double> thogai,
  Value<String> seluthumMurai,
  Value<String?> vangiPeyar,
  Value<String?> parivarthanaiEn,
  Value<String> ullkurippu,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});
typedef $$KooliPatrugalTableTableUpdateCompanionBuilder
    = KooliPatrugalTableCompanion Function({
  Value<int> id,
  Value<int?> niruvanamId,
  Value<String> patruEn,
  Value<String> finYear,
  Value<int> vanakkam,
  Value<int?> vaangunarId,
  Value<Map<String, String>> vaangunarPeyar,
  Value<Map<String, String>> vaangunarMunvari,
  Value<DateTime> patruNaal,
  Value<double> thogai,
  Value<String> seluthumMurai,
  Value<String?> vangiPeyar,
  Value<String?> parivarthanaiEn,
  Value<String> ullkurippu,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<bool> isDeleted,
  Value<DateTime?> deletedAt,
});

class $$KooliPatrugalTableTableFilterComposer
    extends Composer<_$KooliDatabase, $KooliPatrugalTableTable> {
  $$KooliPatrugalTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get niruvanamId => $composableBuilder(
      column: $table.niruvanamId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get patruEn => $composableBuilder(
      column: $table.patruEn, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get finYear => $composableBuilder(
      column: $table.finYear, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get vanakkam => $composableBuilder(
      column: $table.vanakkam, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get vaangunarId => $composableBuilder(
      column: $table.vaangunarId, builder: (column) => ColumnFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get vaangunarPeyar => $composableBuilder(
          column: $table.vaangunarPeyar,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnWithTypeConverterFilters<Map<String, String>, Map<String, String>,
          String>
      get vaangunarMunvari => $composableBuilder(
          column: $table.vaangunarMunvari,
          builder: (column) => ColumnWithTypeConverterFilters(column));

  ColumnFilters<DateTime> get patruNaal => $composableBuilder(
      column: $table.patruNaal, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get thogai => $composableBuilder(
      column: $table.thogai, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get seluthumMurai => $composableBuilder(
      column: $table.seluthumMurai, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get vangiPeyar => $composableBuilder(
      column: $table.vangiPeyar, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get parivarthanaiEn => $composableBuilder(
      column: $table.parivarthanaiEn,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get ullkurippu => $composableBuilder(
      column: $table.ullkurippu, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnFilters(column));
}

class $$KooliPatrugalTableTableOrderingComposer
    extends Composer<_$KooliDatabase, $KooliPatrugalTableTable> {
  $$KooliPatrugalTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get niruvanamId => $composableBuilder(
      column: $table.niruvanamId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get patruEn => $composableBuilder(
      column: $table.patruEn, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get finYear => $composableBuilder(
      column: $table.finYear, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get vanakkam => $composableBuilder(
      column: $table.vanakkam, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get vaangunarId => $composableBuilder(
      column: $table.vaangunarId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get vaangunarPeyar => $composableBuilder(
      column: $table.vaangunarPeyar,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get vaangunarMunvari => $composableBuilder(
      column: $table.vaangunarMunvari,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get patruNaal => $composableBuilder(
      column: $table.patruNaal, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get thogai => $composableBuilder(
      column: $table.thogai, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get seluthumMurai => $composableBuilder(
      column: $table.seluthumMurai,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get vangiPeyar => $composableBuilder(
      column: $table.vangiPeyar, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get parivarthanaiEn => $composableBuilder(
      column: $table.parivarthanaiEn,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get ullkurippu => $composableBuilder(
      column: $table.ullkurippu, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isDeleted => $composableBuilder(
      column: $table.isDeleted, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get deletedAt => $composableBuilder(
      column: $table.deletedAt, builder: (column) => ColumnOrderings(column));
}

class $$KooliPatrugalTableTableAnnotationComposer
    extends Composer<_$KooliDatabase, $KooliPatrugalTableTable> {
  $$KooliPatrugalTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<int> get niruvanamId => $composableBuilder(
      column: $table.niruvanamId, builder: (column) => column);

  GeneratedColumn<String> get patruEn =>
      $composableBuilder(column: $table.patruEn, builder: (column) => column);

  GeneratedColumn<String> get finYear =>
      $composableBuilder(column: $table.finYear, builder: (column) => column);

  GeneratedColumn<int> get vanakkam =>
      $composableBuilder(column: $table.vanakkam, builder: (column) => column);

  GeneratedColumn<int> get vaangunarId => $composableBuilder(
      column: $table.vaangunarId, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String>
      get vaangunarPeyar => $composableBuilder(
          column: $table.vaangunarPeyar, builder: (column) => column);

  GeneratedColumnWithTypeConverter<Map<String, String>, String>
      get vaangunarMunvari => $composableBuilder(
          column: $table.vaangunarMunvari, builder: (column) => column);

  GeneratedColumn<DateTime> get patruNaal =>
      $composableBuilder(column: $table.patruNaal, builder: (column) => column);

  GeneratedColumn<double> get thogai =>
      $composableBuilder(column: $table.thogai, builder: (column) => column);

  GeneratedColumn<String> get seluthumMurai => $composableBuilder(
      column: $table.seluthumMurai, builder: (column) => column);

  GeneratedColumn<String> get vangiPeyar => $composableBuilder(
      column: $table.vangiPeyar, builder: (column) => column);

  GeneratedColumn<String> get parivarthanaiEn => $composableBuilder(
      column: $table.parivarthanaiEn, builder: (column) => column);

  GeneratedColumn<String> get ullkurippu => $composableBuilder(
      column: $table.ullkurippu, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);

  GeneratedColumn<bool> get isDeleted =>
      $composableBuilder(column: $table.isDeleted, builder: (column) => column);

  GeneratedColumn<DateTime> get deletedAt =>
      $composableBuilder(column: $table.deletedAt, builder: (column) => column);
}

class $$KooliPatrugalTableTableTableManager extends RootTableManager<
    _$KooliDatabase,
    $KooliPatrugalTableTable,
    KooliPatrugalEntry,
    $$KooliPatrugalTableTableFilterComposer,
    $$KooliPatrugalTableTableOrderingComposer,
    $$KooliPatrugalTableTableAnnotationComposer,
    $$KooliPatrugalTableTableCreateCompanionBuilder,
    $$KooliPatrugalTableTableUpdateCompanionBuilder,
    (
      KooliPatrugalEntry,
      BaseReferences<_$KooliDatabase, $KooliPatrugalTableTable,
          KooliPatrugalEntry>
    ),
    KooliPatrugalEntry,
    PrefetchHooks Function()> {
  $$KooliPatrugalTableTableTableManager(
      _$KooliDatabase db, $KooliPatrugalTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$KooliPatrugalTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$KooliPatrugalTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$KooliPatrugalTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<int?> niruvanamId = const Value.absent(),
            Value<String> patruEn = const Value.absent(),
            Value<String> finYear = const Value.absent(),
            Value<int> vanakkam = const Value.absent(),
            Value<int?> vaangunarId = const Value.absent(),
            Value<Map<String, String>> vaangunarPeyar = const Value.absent(),
            Value<Map<String, String>> vaangunarMunvari = const Value.absent(),
            Value<DateTime> patruNaal = const Value.absent(),
            Value<double> thogai = const Value.absent(),
            Value<String> seluthumMurai = const Value.absent(),
            Value<String?> vangiPeyar = const Value.absent(),
            Value<String?> parivarthanaiEn = const Value.absent(),
            Value<String> ullkurippu = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliPatrugalTableCompanion(
            id: id,
            niruvanamId: niruvanamId,
            patruEn: patruEn,
            finYear: finYear,
            vanakkam: vanakkam,
            vaangunarId: vaangunarId,
            vaangunarPeyar: vaangunarPeyar,
            vaangunarMunvari: vaangunarMunvari,
            patruNaal: patruNaal,
            thogai: thogai,
            seluthumMurai: seluthumMurai,
            vangiPeyar: vangiPeyar,
            parivarthanaiEn: parivarthanaiEn,
            ullkurippu: ullkurippu,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<int?> niruvanamId = const Value.absent(),
            required String patruEn,
            Value<String> finYear = const Value.absent(),
            Value<int> vanakkam = const Value.absent(),
            Value<int?> vaangunarId = const Value.absent(),
            Value<Map<String, String>> vaangunarPeyar = const Value.absent(),
            Value<Map<String, String>> vaangunarMunvari = const Value.absent(),
            Value<DateTime> patruNaal = const Value.absent(),
            Value<double> thogai = const Value.absent(),
            Value<String> seluthumMurai = const Value.absent(),
            Value<String?> vangiPeyar = const Value.absent(),
            Value<String?> parivarthanaiEn = const Value.absent(),
            Value<String> ullkurippu = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<bool> isDeleted = const Value.absent(),
            Value<DateTime?> deletedAt = const Value.absent(),
          }) =>
              KooliPatrugalTableCompanion.insert(
            id: id,
            niruvanamId: niruvanamId,
            patruEn: patruEn,
            finYear: finYear,
            vanakkam: vanakkam,
            vaangunarId: vaangunarId,
            vaangunarPeyar: vaangunarPeyar,
            vaangunarMunvari: vaangunarMunvari,
            patruNaal: patruNaal,
            thogai: thogai,
            seluthumMurai: seluthumMurai,
            vangiPeyar: vangiPeyar,
            parivarthanaiEn: parivarthanaiEn,
            ullkurippu: ullkurippu,
            createdAt: createdAt,
            updatedAt: updatedAt,
            isDeleted: isDeleted,
            deletedAt: deletedAt,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$KooliPatrugalTableTableProcessedTableManager = ProcessedTableManager<
    _$KooliDatabase,
    $KooliPatrugalTableTable,
    KooliPatrugalEntry,
    $$KooliPatrugalTableTableFilterComposer,
    $$KooliPatrugalTableTableOrderingComposer,
    $$KooliPatrugalTableTableAnnotationComposer,
    $$KooliPatrugalTableTableCreateCompanionBuilder,
    $$KooliPatrugalTableTableUpdateCompanionBuilder,
    (
      KooliPatrugalEntry,
      BaseReferences<_$KooliDatabase, $KooliPatrugalTableTable,
          KooliPatrugalEntry>
    ),
    KooliPatrugalEntry,
    PrefetchHooks Function()>;
typedef $$KooliPatruPattiyalTableTableCreateCompanionBuilder
    = KooliPatruPattiyalTableCompanion Function({
  Value<int> id,
  required int patruId,
  required int pattiyalId,
  Value<double> poruthiyaThogai,
});
typedef $$KooliPatruPattiyalTableTableUpdateCompanionBuilder
    = KooliPatruPattiyalTableCompanion Function({
  Value<int> id,
  Value<int> patruId,
  Value<int> pattiyalId,
  Value<double> poruthiyaThogai,
});

class $$KooliPatruPattiyalTableTableFilterComposer
    extends Composer<_$KooliDatabase, $KooliPatruPattiyalTableTable> {
  $$KooliPatruPattiyalTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get patruId => $composableBuilder(
      column: $table.patruId, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get pattiyalId => $composableBuilder(
      column: $table.pattiyalId, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get poruthiyaThogai => $composableBuilder(
      column: $table.poruthiyaThogai,
      builder: (column) => ColumnFilters(column));
}

class $$KooliPatruPattiyalTableTableOrderingComposer
    extends Composer<_$KooliDatabase, $KooliPatruPattiyalTableTable> {
  $$KooliPatruPattiyalTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get patruId => $composableBuilder(
      column: $table.patruId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get pattiyalId => $composableBuilder(
      column: $table.pattiyalId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get poruthiyaThogai => $composableBuilder(
      column: $table.poruthiyaThogai,
      builder: (column) => ColumnOrderings(column));
}

class $$KooliPatruPattiyalTableTableAnnotationComposer
    extends Composer<_$KooliDatabase, $KooliPatruPattiyalTableTable> {
  $$KooliPatruPattiyalTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<int> get patruId =>
      $composableBuilder(column: $table.patruId, builder: (column) => column);

  GeneratedColumn<int> get pattiyalId => $composableBuilder(
      column: $table.pattiyalId, builder: (column) => column);

  GeneratedColumn<double> get poruthiyaThogai => $composableBuilder(
      column: $table.poruthiyaThogai, builder: (column) => column);
}

class $$KooliPatruPattiyalTableTableTableManager extends RootTableManager<
    _$KooliDatabase,
    $KooliPatruPattiyalTableTable,
    KooliPatruPattiyalEntry,
    $$KooliPatruPattiyalTableTableFilterComposer,
    $$KooliPatruPattiyalTableTableOrderingComposer,
    $$KooliPatruPattiyalTableTableAnnotationComposer,
    $$KooliPatruPattiyalTableTableCreateCompanionBuilder,
    $$KooliPatruPattiyalTableTableUpdateCompanionBuilder,
    (
      KooliPatruPattiyalEntry,
      BaseReferences<_$KooliDatabase, $KooliPatruPattiyalTableTable,
          KooliPatruPattiyalEntry>
    ),
    KooliPatruPattiyalEntry,
    PrefetchHooks Function()> {
  $$KooliPatruPattiyalTableTableTableManager(
      _$KooliDatabase db, $KooliPatruPattiyalTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$KooliPatruPattiyalTableTableFilterComposer(
                  $db: db, $table: table),
          createOrderingComposer: () =>
              $$KooliPatruPattiyalTableTableOrderingComposer(
                  $db: db, $table: table),
          createComputedFieldComposer: () =>
              $$KooliPatruPattiyalTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<int> patruId = const Value.absent(),
            Value<int> pattiyalId = const Value.absent(),
            Value<double> poruthiyaThogai = const Value.absent(),
          }) =>
              KooliPatruPattiyalTableCompanion(
            id: id,
            patruId: patruId,
            pattiyalId: pattiyalId,
            poruthiyaThogai: poruthiyaThogai,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            required int patruId,
            required int pattiyalId,
            Value<double> poruthiyaThogai = const Value.absent(),
          }) =>
              KooliPatruPattiyalTableCompanion.insert(
            id: id,
            patruId: patruId,
            pattiyalId: pattiyalId,
            poruthiyaThogai: poruthiyaThogai,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$KooliPatruPattiyalTableTableProcessedTableManager
    = ProcessedTableManager<
        _$KooliDatabase,
        $KooliPatruPattiyalTableTable,
        KooliPatruPattiyalEntry,
        $$KooliPatruPattiyalTableTableFilterComposer,
        $$KooliPatruPattiyalTableTableOrderingComposer,
        $$KooliPatruPattiyalTableTableAnnotationComposer,
        $$KooliPatruPattiyalTableTableCreateCompanionBuilder,
        $$KooliPatruPattiyalTableTableUpdateCompanionBuilder,
        (
          KooliPatruPattiyalEntry,
          BaseReferences<_$KooliDatabase, $KooliPatruPattiyalTableTable,
              KooliPatruPattiyalEntry>
        ),
        KooliPatruPattiyalEntry,
        PrefetchHooks Function()>;

class $KooliDatabaseManager {
  final _$KooliDatabase _db;
  $KooliDatabaseManager(this._db);
  $$KooliNiruvanaTharavugalTableTableTableManager
      get kooliNiruvanaTharavugalTable =>
          $$KooliNiruvanaTharavugalTableTableTableManager(
              _db, _db.kooliNiruvanaTharavugalTable);
  $$KooliVaangunarTableTableTableManager get kooliVaangunarTable =>
      $$KooliVaangunarTableTableTableManager(_db, _db.kooliVaangunarTable);
  $$KooliPorulTableTableTableManager get kooliPorulTable =>
      $$KooliPorulTableTableTableManager(_db, _db.kooliPorulTable);
  $$KooliPattiyalTableTableTableManager get kooliPattiyalTable =>
      $$KooliPattiyalTableTableTableManager(_db, _db.kooliPattiyalTable);
  $$KooliPatrugalTableTableTableManager get kooliPatrugalTable =>
      $$KooliPatrugalTableTableTableManager(_db, _db.kooliPatrugalTable);
  $$KooliPatruPattiyalTableTableTableManager get kooliPatruPattiyalTable =>
      $$KooliPatruPattiyalTableTableTableManager(
          _db, _db.kooliPatruPattiyalTable);
}
