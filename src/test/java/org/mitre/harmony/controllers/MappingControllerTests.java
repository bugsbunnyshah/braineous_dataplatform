package org.mitre.harmony.controllers;

import org.junit.jupiter.api.Test;
import org.mitre.schemastore.model.MappingCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MappingControllerTests {
    private static Logger logger = LoggerFactory.getLogger(MappingControllerTests.class);

    @Test
    public void testGetMappingCellIDs() throws Exception
    {
        List<MappingCell> mappingCells = new ArrayList<>();

        for (int i=0; i<5; i++) {
            MappingCell mappingCell = new MappingCell();
            mappingCell.setId(i);
            mappingCell.setAuthor("blah");
            mappingCells.add(mappingCell);
        }

        ArrayList<Integer> ids = MappingController.getMappingCellIDs(mappingCells);
        logger.info(ids.toString());


    }
}
