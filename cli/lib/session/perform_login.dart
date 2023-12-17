import 'dart:convert';
import 'dart:io';

import 'package:cli/session/session.dart';

import '../create_tenant_command.dart';

class PerformLogin{

  Future<void> startLogin() async {
    Session session = Session.session;
    print("> If you have a tenant press [l], If you need to create a tenant press[c]");
    var option = stdin.readLineSync(encoding: utf8);
    if(option == 'l'){
      var arguments = [];

      print("> tenant: ");
      var tenant = stdin.readLineSync(encoding: utf8);
      session.tenant = tenant!;

      print("> email: ");
      var email = stdin.readLineSync(encoding: utf8);
      session.email = email!;

      print("> password: ");
      var password = stdin.readLineSync(encoding: utf8);

      arguments.add(tenant);
      arguments.add(email);
      arguments.add(password);

      //TODO: login
    }else{
      CreateTenantCommand createTenantCommand = CreateTenantCommand();
      var arguments = [];

      print("Create a tenant");
      print("> tenant: ");
      var tenant = stdin.readLineSync(encoding: utf8);
      session.tenant = tenant!;

      print("> email: ");
      var email = stdin.readLineSync(encoding: utf8);
      session.email = email!;

      print("> password: ");
      var password = stdin.readLineSync(encoding: utf8);

      arguments.add(tenant);
      arguments.add(email);
      arguments.add(password);

      Map<String,dynamic> credentials = await createTenantCommand.execute(arguments);
      print(credentials);
    }
  }

}