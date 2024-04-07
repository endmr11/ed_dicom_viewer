import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'ed_dicom_viewer_method_channel.dart';

abstract class EdDicomViewerPlatform extends PlatformInterface {
  EdDicomViewerPlatform() : super(token: _token);

  static final Object _token = Object();

  static EdDicomViewerPlatform _instance = EdMethodChannelDicomViewer();

  static EdDicomViewerPlatform get instance => _instance;

  static set instance(EdDicomViewerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String> getViewDicom(String filePath) {
    throw UnimplementedError('getViewDicom() has not been implemented.');
  }
}
