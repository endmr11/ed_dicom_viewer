import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:ed_dicom_viewer/ed_dicom_viewer.dart';
import 'package:file_selector/file_selector.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _dicomViewerPlugin = EdDicomViewer();
  final typeGroup = const XTypeGroup(
    label: 'dicom',
    extensions: <String>['dcm'],
  );
  Uint8List? decodedData;
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Dicom Viewer'),
        ),
        body: Column(
          children: [
            ElevatedButton(
              onPressed: () async {
                final XFile? file = await openFile(acceptedTypeGroups: <XTypeGroup>[typeGroup]);

                final Directory tempDir = await getTemporaryDirectory();
                var newFile = File('${tempDir.path}/example2.dcm')..writeAsBytesSync(await file!.readAsBytes());
                print(newFile.path);
                _dicomViewerPlugin.getViewDicom(newFile.path).then((value) {
                  EdDicomModel dicomModel = EdDicomModel.fromJson(jsonDecode(value));
                  print(dicomModel.toString());
                  setState(() {
                    decodedData = Uint8List.fromList(dicomModel.decodedBytes);
                  });
                });
              },
              child: const Text("Select File"),
            ),
            if (decodedData != null) ...[
              SizedBox(
                width: 300,
                height: 300,
                child: Image.memory(decodedData!),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
