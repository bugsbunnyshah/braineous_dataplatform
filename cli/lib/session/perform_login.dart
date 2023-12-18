import 'dart:convert';
import 'dart:io';

import 'package:cli/authenticate_tenant_command.dart';
import 'package:cli/command_registry.dart';
import 'package:cli/move_pipe_to_dev_command.dart';
import 'package:cli/session/session.dart';

import '../create_tenant_command.dart';
import '../list_allpipes_command.dart';
import '../move_pipe_to_deployed_command.dart';
import '../move_pipe_to_staging_command.dart';

class PerformLogin{

  Future<void> startLogin() async {
    Session session = Session.session;
    print("> If you have a tenant press [l] to login, If you need to create a tenant press[c]");
    var option = stdin.readLineSync(encoding: utf8);
    if(option == 'l'){
      AuthenticateTenantCommand command = AuthenticateTenantCommand();
      var arguments = [];

      print("Login");
      print("> email: ");
      var email = stdin.readLineSync(encoding: utf8);
      session.email = email!;

      print("> password: ");
      var password = stdin.readLineSync(encoding: utf8);

      arguments.add(email);
      arguments.add(password);

      //login
      await command.execute(arguments);
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

      await createTenantCommand.execute(arguments);
    }

    var headers = [];
    headers.add(session.apiKey);
    headers.add(session.apiSecret);

    //TODO: collect from user
    String pipeName = "medical_records";
    headers.add(pipeName);

    //show pipes
    ListAllPipesCommand allPipesCommand = CommandRegistry.registry.commands['show pipes'];
    await allPipesCommand.execute(headers);

    //move pipe_to_development
    MovePipeToDevCommand movePipeToDevCommand = CommandRegistry.registry.commands['move pipe_to_development'];
    await movePipeToDevCommand.execute(headers);

    //move pipe_to_staging
    MovePipeToStagingCommand movePipeToStagingCommand = CommandRegistry.registry.commands['move pipe_to_staging'];
    await movePipeToStagingCommand.execute(headers);

    //move pipe_to_production
    MovePipeToDeployedCommand movePipeToDeployedCommand = CommandRegistry.registry.commands['move pipe_to_production'];
    await movePipeToDeployedCommand.execute(headers);
  }

}