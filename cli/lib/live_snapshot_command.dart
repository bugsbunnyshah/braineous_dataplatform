import 'dart:convert';

import 'package:cli/session/session.dart';
import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:http/http.dart' as http;

class LiveSnapshotCommand {

  Future<void> execute(List<dynamic> arguments) async {
    try {
      //
      RestInvocationResponse invocationResponse = await invokeRestEndpoint(
          arguments);

      List<dynamic> result = invocationResponse.json;
      print("*******Live_Snapshot********");
      print(result);
    }on RestInvocationException catch (_, e){
      print(_.json);
    }
  }
}

Future<RestInvocationResponse> invokeRestEndpoint(List<dynamic> arguments) async {
  Session session = Session.session;
  String host = session.host;
  String port = session.port;
  String hostUrl = "$host:$port";
  String pipeName = session.usingPipe;
  final url = Uri.http(hostUrl, '/pipeline_manager/live_snapshot/');

  String clientIp = "127.00.1";
  String snapshotId = "snapshotId";

  Map<String,dynamic> jsonMap = {};
  jsonMap['clientIp'] = clientIp;
  jsonMap['snapshotId'] = snapshotId;
  jsonMap['pipeName'] = pipeName;
  String json = jsonEncode(jsonMap);

  final response = await http.post(url,
      headers: {
        "x-api-key":arguments[0],
        "x-api-key-secret": arguments[1],
      },
      body: json);

  // If the request didn't succeed, throw an exception
  if (response.statusCode != 200) {
    Map<String,dynamic> responseJsonMap = {};
    if(response.body.trim() != "") {
      responseJsonMap = jsonDecode(response.body) as Map<String, dynamic>;
    }

    responseJsonMap["statusCode"] = response.statusCode;
    throw RestInvocationException(
        statusCode: response.statusCode, responseJsonMap
    );
  }

  final responseJson = jsonDecode(response.body) as List<dynamic>;

  return RestInvocationResponse.fromJson(responseJson);
}

class RestInvocationResponse {
  final List<dynamic> json;

  RestInvocationResponse({
    required this.json,
  });

  factory RestInvocationResponse.fromJson(List<dynamic> json) {
    return RestInvocationResponse(
      json: json,
    );
  }
}

class RestInvocationException implements Exception {
  final int? statusCode;
  final dynamic json;

  RestInvocationException(this.json, {this.statusCode});

  @override
  String toString() {
    final buf = StringBuffer();
    buf.write('Failed to invoke endpoint');

    if (statusCode != null) {
      buf.write(' with a status code of $statusCode');
    }

    buf.write('!');
    return buf.toString();
  }
}