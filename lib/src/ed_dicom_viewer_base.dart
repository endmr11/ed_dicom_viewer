import 'ed_dicom_viewer_platform_interface.dart';

class EdDicomViewer {
  Future<String> getViewDicom(String filePath) {
    return EdDicomViewerPlatform.instance.getViewDicom(filePath);
  }
}
