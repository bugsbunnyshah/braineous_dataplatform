import 'dart:convert';

import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:http/http.dart' as http;

class ListDeployedPipesCommand {

  Future<String> execute(List<dynamic> arguments) async {
    String message = "";

    //
    RestInvocationResponse invocationResponse = await invokeRestEndpoint(arguments);
    message += '${invocationResponse.json}';

    var unicode = art.renderUnicode(message, art.UnicodeFont.doublestruck);

    //return unicode.toString();

    return message;
  }
}

Future<RestInvocationResponse> invokeRestEndpoint(List<dynamic> arguments) async {
  final url = Uri.http('localhost:8080', '/pipeline_manager/deployed_pipes/');

  final response = await http.get(url,headers: {
    "x-api-key":"0132d8be-c85c-423a-a168-4767f4dd638b",
    "x-api-key-secret": "d8e452ea-9968-434c-b84c-5276781a60b6",
  },);

  // If the request didn't succeed, throw an exception
  if (response.statusCode != 200) {
    throw RestInvocationException(
      statusCode: response.statusCode,
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

  RestInvocationException({this.statusCode});

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