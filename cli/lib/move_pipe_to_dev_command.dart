import 'dart:convert';

import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:http/http.dart' as http;

class MovePipeToDevCommand {

  Future<String> execute(List<dynamic> arguments) async {
    String message = "";

    //
    RestInvocationResponse invocationResponse = await invokeRestEndpoint(arguments);
    message += '${invocationResponse.message}';
    message += '\n${invocationResponse.json}';

    var unicode = art.renderUnicode(message, art.UnicodeFont.doublestruck);

    //return unicode.toString();

    return message;
  }
}

Future<RestInvocationResponse> invokeRestEndpoint(List<dynamic> arguments) async {
  final url = Uri.http('localhost:8080', '/pipeline_manager/move_to_development/');

  Map<String,dynamic> jsonMap = {};
  jsonMap['pipeName'] = arguments[0];
  String json = jsonEncode(jsonMap);

  final response = await http.post(url,body: json);

  // If the request didn't succeed, throw an exception
  if (response.statusCode != 200) {
    throw RestInvocationException(
      statusCode: response.statusCode,
    );
  }

  final responseJson = jsonDecode(response.body) as Map<String, dynamic>;

  return RestInvocationResponse.fromJson(responseJson);
}

class RestInvocationResponse {
  final String message;
  final dynamic json;

  RestInvocationResponse({
    required this.message,
    required this.json,
  });

  factory RestInvocationResponse.fromJson(Map<String, dynamic> json) {
    return RestInvocationResponse(
      message: json['message'] as String,
      json: json['pipe'],
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