import 'dart:convert';

import 'package:cli/delivery_stats_command.dart';
import 'package:cli/ingestion_stats_command.dart';
import 'package:cli/list_allpipes_command.dart';
import 'package:cli/list_deployedpipes_command.dart';
import 'package:cli/list_devpipes_command.dart';
import 'package:cli/list_stagedpipes_command.dart';
import 'package:cli/live_snapshot_command.dart';
import 'package:cli/move_pipe_to_deployed_command.dart';
import 'package:cli/move_pipe_to_staging_command.dart';
import 'package:enough_ascii_art/enough_ascii_art.dart' as art;
import 'package:cli/move_pipe_to_dev_command.dart';

void main() async {
  String message = "********************\n BRAINEOUS \n *****************";
  var unicode = art.renderUnicode(message, art.UnicodeFont.doublestruck);
  print(unicode);

  //execute command
  var arguments = [];
  arguments.add("flights");

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
  try {
    var params = [];
    params.add("blah");
    LiveSnapshotCommand liveSnapshot404Command = LiveSnapshotCommand();
    String liveSnapshot404CommandResponse = await liveSnapshot404Command
        .execute(params);
    print(liveSnapshot404CommandResponse);
  } catch(e){
    print('Exception: $e');
  }


  print("*********************************************************************");
  IngestionStatsCommand ingestionStatsCommand = IngestionStatsCommand();
  String ingestionStatsCommandResponse = await ingestionStatsCommand.execute(arguments);
  print(ingestionStatsCommandResponse);

  print("*********************************************************************");
  try {
    var params = [];
    params.add("blah");
    IngestionStatsCommand ingestionStats404Command = IngestionStatsCommand();
    String ingestionStats404CommandResponse = await ingestionStats404Command
        .execute(params);
    print(ingestionStats404CommandResponse);
  } catch(e){
    print('Exception: $e');
  }

  print("*********************************************************************");
  DeliveryStatsCommand deliveryStatsCommand = DeliveryStatsCommand();
  String deliveryStatsCommandResponse = await deliveryStatsCommand.execute(arguments);
  print(deliveryStatsCommandResponse);

  print("*********************************************************************");
  try {
    var params = [];
    params.add("blah");
    DeliveryStatsCommand deliveryStats404Command = DeliveryStatsCommand();
    String deliveryStats404CommandResponse = await deliveryStats404Command
        .execute(params);
    print(deliveryStats404CommandResponse);
  } catch(e){
    print('Exception: $e');
  }

  print("*********************************************************************");
}