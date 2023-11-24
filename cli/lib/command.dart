import 'dart:convert';

import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:http/http.dart' as http;

class Command {

  Future<String> execute(List<dynamic> arguments) async {
    String message = arguments.toString();

    //
    RestInvocationResponse packageInfo = await invokeRestEndpoint('http');
    message += '\n Information about the ${packageInfo.latestVersion} :';
    message += '\n Description ${packageInfo.description} :';

    var unicode = art.renderUnicode(message, art.UnicodeFont.doublestruck);

    return unicode.toString();
  }
}

Future<RestInvocationResponse> invokeRestEndpoint(String packageName) async {
  final packageUrl = Uri.https('dart.dev', '/f/packages/$packageName.json');
  final packageResponse = await http.get(packageUrl);

  // If the request didn't succeed, throw an exception
  if (packageResponse.statusCode != 200) {
    throw RestInvocationException(
      packageName: packageName,
      statusCode: packageResponse.statusCode,
    );
  }

  final packageJson = json.decode(packageResponse.body) as Map<String, dynamic>;

  return RestInvocationResponse.fromJson(packageJson);
}

class RestInvocationResponse {
  final String name;
  final String latestVersion;
  final String description;
  final String publisher;
  final Uri? repository;

  RestInvocationResponse({
    required this.name,
    required this.latestVersion,
    required this.description,
    required this.publisher,
    this.repository,
  });

  factory RestInvocationResponse.fromJson(Map<String, dynamic> json) {
    final repository = json['repository'] as String?;

    return RestInvocationResponse(
      name: json['name'] as String,
      latestVersion: json['latestVersion'] as String,
      description: json['description'] as String,
      publisher: json['publisher'] as String,
      repository: repository != null ? Uri.tryParse(repository) : null,
    );
  }
}

class RestInvocationException implements Exception {
  final String packageName;
  final int? statusCode;

  RestInvocationException({required this.packageName, this.statusCode});

  @override
  String toString() {
    final buf = StringBuffer();
    buf.write('Failed to retrieve package:$packageName information');

    if (statusCode != null) {
      buf.write(' with a status code of $statusCode');
    }

    buf.write('!');
    return buf.toString();
  }
}