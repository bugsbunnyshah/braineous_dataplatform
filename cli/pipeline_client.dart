import 'dart:convert';
import 'dart:io';

import 'package:cli/create_tenant_command.dart';
import 'package:cli/delivery_stats_command.dart';
import 'package:cli/ingestion_stats_command.dart';
import 'package:cli/list_allpipes_command.dart';
import 'package:cli/list_deployedpipes_command.dart';
import 'package:cli/list_devpipes_command.dart';
import 'package:cli/list_stagedpipes_command.dart';
import 'package:cli/live_snapshot_command.dart';
import 'package:cli/move_pipe_to_deployed_command.dart';
import 'package:cli/move_pipe_to_staging_command.dart';
import 'package:cli/session/perform_login.dart';
import 'package:cli/session/session.dart';
import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:cli/move_pipe_to_dev_command.dart';

import 'package:args/args.dart';

void main(List<String> arguments) async {
  String message = "********************\n BRAINEOUS \n *****************";
  var unicode = art.renderUnicode(message, art.UnicodeFont.doublestruck);
  print(unicode);

  final parser = ArgParser();
  parser.addOption("host", defaultsTo: "localhost");
  parser.addOption("port", defaultsTo: "8080");

  var results = parser.parse(arguments);
  Session.session.host = results['host'];
  Session.session.port = results['port'];

  PerformLogin performLogin = PerformLogin();
  performLogin.startLogin();

  /*
  //execute command
  var arguments = [];
  arguments.add("flights");

  String apiKey;
  String apiSecret;
  //create tenant
  var createTenantArgs = [];
  createTenantArgs.add("test");
  createTenantArgs.add("test@email.com");
  CreateTenantCommand createTenantCommand = CreateTenantCommand();
  Map<String,dynamic> credentials = await createTenantCommand.execute(createTenantArgs);
  apiKey = credentials['apiKey'];
  apiSecret = credentials['apiSecret'];
  print(apiKey);
  print(apiSecret);
  arguments.add(apiKey);
  arguments.add(apiSecret);

  MovePipeToDevCommand movePipeToDevCommand = MovePipeToDevCommand();
  String moveResponse = await movePipeToDevCommand.execute(arguments);
  print(moveResponse);

  MovePipeToStagingCommand movePipeToStagingCommand = MovePipeToStagingCommand();
  String moveToStagingResponse = await movePipeToStagingCommand.execute(arguments);
  print(moveToStagingResponse);

  MovePipeToDeployedCommand movePipeToDeployedCommand = MovePipeToDeployedCommand();
  String moveToDeployedResponse = await movePipeToDeployedCommand.execute(arguments);
  print(moveToDeployedResponse);

  print("*********************************************************************");
  ListAllPipesCommand allPipesCommand = ListAllPipesCommand();
  String allPipesResponse = await allPipesCommand.execute(arguments);
  print(allPipesResponse);

  print("*********************************************************************");
  ListDevPipesCommand devPipesCommand = ListDevPipesCommand();
  String devPipesResponse = await devPipesCommand.execute(arguments);
  print(devPipesResponse);

  print("*********************************************************************");
  ListStagedPipesCommand stagedPipesCommand = ListStagedPipesCommand();
  String stagedPipesResponse = await stagedPipesCommand.execute(arguments);
  print(stagedPipesResponse);

  print("*********************************************************************");
  ListDeployedPipesCommand deployedPipesCommand = ListDeployedPipesCommand();
  String deployedPipesResponse = await deployedPipesCommand.execute(arguments);
  print(deployedPipesResponse);

  print("*********************************************************************");
  LiveSnapshotCommand liveSnapshotCommand = LiveSnapshotCommand();
  String liveSnapshotCommandResponse = await liveSnapshotCommand.execute(arguments);
  print(liveSnapshotCommandResponse);

  print("*********************************************************************");
  IngestionStatsCommand ingestionStatsCommand = IngestionStatsCommand();
  String ingestionStatsCommandResponse = await ingestionStatsCommand.execute(arguments);
  print(ingestionStatsCommandResponse);

  print("*********************************************************************");
  DeliveryStatsCommand deliveryStatsCommand = DeliveryStatsCommand();
  String deliveryStatsCommandResponse = await deliveryStatsCommand.execute(arguments);
  print(deliveryStatsCommandResponse);

  print("*********************************************************************");
  try {
    arguments[0] = "blah";
    LiveSnapshotCommand liveSnapshot404Command = LiveSnapshotCommand();
    String liveSnapshot404CommandResponse = await liveSnapshot404Command
        .execute(arguments);
    print(liveSnapshot404CommandResponse);
  } catch(e){
    print('Exception: $e');
  }

  print("*********************************************************************");
  try {
    arguments[0] = "blah";
    IngestionStatsCommand ingestionStats404Command = IngestionStatsCommand();
    String ingestionStats404CommandResponse = await ingestionStats404Command
        .execute(arguments);
    print(ingestionStats404CommandResponse);
  } catch(e){
    print('Exception: $e');
  }

  print("*********************************************************************");
  try {
    arguments[0] = "blah";
    DeliveryStatsCommand deliveryStats404Command = DeliveryStatsCommand();
    String deliveryStats404CommandResponse = await deliveryStats404Command
        .execute(arguments);
    print(deliveryStats404CommandResponse);
  } catch(e){
    print('Exception: $e');
  }

  print("*********************************************************************");*/
}