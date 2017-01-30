#!/bin/sh
#
# Copyright (c) 2016 EMC Corporation
# All Rights Reserved
#
# run these by executing dutests.sh with one of the test function names below as the test arg
#


# Export Test VPLEX_ORCH_1
#
# Summary: Remove Initiator should not remove volumes unless validation_check is off.
#
# 1. ViPR creates 2 volumes, 1 cluster export to two hosts.
# 2. turn on validation_check flag
# 3. remove all initiators for host1
#     -- expected result: exception with error message about how volumes would be removed
# 4. ? verify no zones removed
# 5. turn off validation_check flag
# 6. remove all initiators for host1, again
#     -- expected result: host is removed
#
test_VPLEX_ORCH_1() {
    echot "Test VPLEX_ORCH_1: Remove Initiator should not remove volumes unless validation_check is off."
    expname=${EXPORT_GROUP_NAME}tvo1

    # Make sure we start clean; no masking view on the array
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}1 ${HOST2} gone

    set_validation_check true

    # Create the cluster export and masks with the 2 volumes
    runcmd export_group create $PROJECT ${expname}1 $NH --type Cluster --volspec "${PROJECT}/${VOLNAME}-1,${PROJECT}/${VOLNAME}-2" --clusters "${TENANT}/${CLUSTER}"

    verify_export ${expname}1 ${HOST1} 2 2
    verify_export ${expname}1 ${HOST2} 2 2

    # Run the export group command.  Expect it to fail with validation
    fail export_group update $PROJECT/${expname}1 --remInits ${HOST1}/${H1PI1},${HOST1}/${H1PI2}

    # Verify exports still exist
    verify_export ${expname}1 ${HOST1} 2 2
    verify_export ${expname}1 ${HOST2} 2 2

    # Verify the zone names, as we know them, are on the switch
    load_zones ${HOST1} 
    verify_zones ${FC_ZONE_A:7} exists

    set_validation_check false

    # Run the export group command.  It should succeed since there is no validation
    runcmd export_group update $PROJECT/${expname}1 --remInits ${HOST1}/${H1PI1},${HOST1}/${H1PI2}

    # Delete the export group
    runcmd export_group delete $PROJECT/${expname}1

    # Make sure the mask is gone
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}1 ${HOST2} gone

    verify_no_zones ${FC_ZONE_A:7} ${HOST1}
    
    set_validation_check true
}

# Export Test VPLEX_ORCH_2
#
# Summary: Remove Volume should not remove initiators unless validation_check is off.
#
# 1. ViPR creates 1 volume, 1 host export
# 2. turn on validation_check flag
# 3. remove volume
#     -- expected result: exception with error message about how initiators would be removed
# 4. verify no zones removed
# 5. turn off validation_check flag
# 6. remove volume, again
#     -- expected result: volume is removed successfully
#
test_VPLEX_ORCH_2() {
    echot "Test VPLEX_ORCH_2: Remove Volume should not remove initiators unless validation_check is off."
    expname=${EXPORT_GROUP_NAME}tvo2

    # Make sure we start clean; no masking view on the array
    verify_export ${expname}1 ${HOST1} gone

    set_validation_check true

    # Create the mask with the 1 volume
    runcmd export_group create $PROJECT ${expname}1 $NH --type Host --volspec "${PROJECT}/${VOLNAME}-1" --hosts "${HOST1}"

    verify_export ${expname}1 ${HOST1} 2 1

    # Run the export group command.  Expect it to fail with validation
    fail export_group update $PROJECT/${expname}1 --remVols "${PROJECT}/${VOLNAME}-1"

    # Verify exports still exist
    verify_export ${expname}1 ${HOST1} 2 1

    # Verify the zone names, as we know them, are on the switch
    load_zones ${HOST1} 
    verify_zones ${FC_ZONE_A:7} exists

    set_validation_check false

    # Run the export group command.  It should succeed since there is no validation
    runcmd export_group update $PROJECT/${expname}1 --remVols "${PROJECT}/${VOLNAME}-1"

    # Delete the export group
    runcmd export_group delete $PROJECT/${expname}1

    # Make sure the mask is gone
    verify_export ${expname}1 ${HOST1} gone

    verify_no_zones ${FC_ZONE_A:7} ${HOST1}
    
    set_validation_check true
}

# Export Test VPLEX_ORCH_3
#
# Summary: Delete Export Group should not remove Initiators.
#
# 1. ViPR creates 2 volumes
# 2. Export one volume to a cluster of two hosts
# 3. export other volume to just host1 in the cluster
# 4. delete the cluster export group
#     -- expected result: host1's storage view should have no initiators removed from it
# 5. verify no zones removed
#
test_VPLEX_ORCH_3() {
    echot "Test VPLEX_ORCH_3: Delete Export Group should not remove Initiators."
    expname=${EXPORT_GROUP_NAME}tvo3

    # Make sure we start clean; no masking view on the array
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}2 ${HOST1} gone
    verify_export ${expname}2 ${HOST2} gone

    # Create the host export and mask with the 1st volume
    runcmd export_group create $PROJECT ${expname}1 $NH --type Host --volspec "${PROJECT}/${VOLNAME}-1" --hosts "${HOST1}"

    # Create the cluster export and masks with the 2nd volume
    runcmd export_group create $PROJECT ${expname}2 $NH --type Cluster --volspec "${PROJECT}/${VOLNAME}-2" --clusters "${TENANT}/${CLUSTER}"

    verify_export ${expname}1 ${HOST1} 2 2
    verify_export ${expname}1 ${HOST2} 2 1

    set_validation_check true

    # Delete the cluster export group
    fail export_group delete $PROJECT/${expname}2

    set_validation_check false

    # Delete the cluster export group
    runcmd export_group delete $PROJECT/${expname}2

    # Verify exports still exist
    verify_export ${expname}1 ${HOST1} 2 1
    verify_export ${expname}1 ${HOST2} gone
    
    # Verify the zone names, as we know them, are on the switch
    load_zones ${HOST1} 
    verify_zones ${FC_ZONE_A:7} exists

    # Delete the host export group
    runcmd export_group delete $PROJECT/${expname}1

    # Make sure the masks are gone
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}2 ${HOST1} gone
    verify_export ${expname}2 ${HOST2} gone

    verify_no_zones ${FC_ZONE_A:7} ${HOST1}

    set_validation_check true
}

# Export Test EXISITING_USERADDED_INITS

# Conversion of Existing Initiators to User Added initiators if they are ViPR managed within an export Mask
# 1. Create and Export a Volume- V1 to a Host H1 with two initiators I1 and I2
# 2. Using VPLEX add Initiator I3, I3 to the Initiator Group Associated with the Storage View.
# 3. Create and Export a Volume- V2 to this Host. Verify that the Storage View contains 4 initators and 2 Volumes
# 4. Add I3, I4 to Host H1. The export Mask needs to be updated accordingly as part of the Export Group Update.
# 5. Remove I3 and Verify that the Storage View contains 3 initators and 2 Volumes
# 6. Delete Export Group and verify that the Storage View is gone..

test_EXISITING_USERADDED_INITS() {
    echot "Existing Initiators to User Added Initiators Test Begins"

    #Prepare Host, Initiators and zones
    BASENUM=${BASENUM:=$RANDOM}
    EXISTINGINITTEST=hosttest${BASENUM}
    USERADDEDINIT1=10:00:00:DE:AD:BE:EF:01
    USERADDEDINIT2=10:00:00:DE:AD:BE:EF:02
    EXISTINGINIT3=10:00:00:DE:AD:BE:EF:03
    EXISTINGINIT4=10:00:00:DE:AD:BE:EF:04
    PWWN3=100000DEADBEEF03
    PWWN4=100000DEADBEEF04

    runcmd hosts create ${EXISTINGINITTEST} $TENANT Other ${EXISTINGINITTEST} --port 8111 --cluster ${TENANT}/${CLUSTER}
    runcmd initiator create ${EXISTINGINITTEST} FC $USERADDEDINIT1 --node $USERADDEDINIT1
    runcmd initiator create ${EXISTINGINITTEST} FC $USERADDEDINIT2 --node $USERADDEDINIT2
    runcmd transportzone add $NH/${FC_ZONE_A} $USERADDEDINIT1
    runcmd transportzone add $NH/${FC_ZONE_A} $USERADDEDINIT2

    echot "Creating an export Group and exporting the first volume to initiators 10:00:00:DE:AD:BE:EF:01 and 10:00:00:DE:AD:BE:EF:02"
    EXISTINGINITEGTEST=exinitegtest${BASENUM}
    verify_export $EXISTINGINITEGTEST ${EXISTINGINITTEST} gone

    runcmd export_group create $PROJECT $EXISTINGINITEGTEST $NH --type Host --volspec "${PROJECT}/${VOLNAME}-1" --hosts "${EXISTINGINITTEST}"
    verify_export $EXISTINGINITEGTEST ${EXISTINGINITTEST} 2 1

    echot "Adding initiators 10:00:00:DE:AD:BE:EF:03 and 10:00:00:DE:AD:BE:EF:04 to the Masking View using the CLI"
    VPLEXADDINIT=add_initiator_to_mask
    # We need the storage view name to add the initiators to the storage view outside...
    STORAGEVIEWNAME=`get_masking_view_name ${EXISTINGINITEGTEST} ${EXISTINGINITTEST}`
    # Add initiators to the mask (done differently per array type)
    runcmd vplexhelper.sh $VPLEXADDINIT ${PWWN3} ${STORAGEVIEWNAME}
    runcmd vplexhelper.sh $VPLEXADDINIT ${PWWN4} ${STORAGEVIEWNAME}
    runcmd export_group update $PROJECT/$EXISTINGINITEGTEST --addVols "${PROJECT}/${VOLNAME}-2"
    verify_export $EXISTINGINITEGTEST ${EXISTINGINITTEST} 4 2


    echot "Adding existing initiators 10:00:00:DE:AD:BE:EF:03 and 10:00:00:DE:AD:BE:EF:04 to the Host"
    runcmd transportzone add $NH/${FC_ZONE_A} $EXISTINGINIT3
    runcmd transportzone add $NH/${FC_ZONE_A} $EXISTINGINIT4
    runcmd initiator create ${EXISTINGINITTEST} FC $EXISTINGINIT3 --node $EXISTINGINIT3
    runcmd initiator create ${EXISTINGINITTEST} FC $EXISTINGINIT4 --node $EXISTINGINIT4

    echot "Deleting existing initiators 10:00:00:DE:AD:BE:EF:03"
    runcmd initiator delete $EXISTINGINITTEST/$EXISTINGINIT3
    verify_export $EXISTINGINITEGTEST ${EXISTINGINITTEST} 3 2

    echot "Deleting Export Group"
    runcmd export_group delete $PROJECT/$EXISTINGINITEGTEST
    verify_export $EXISTINGINITEGTEST ${EXISTINGINITTEST} gone

    runcmd initiator delete $EXISTINGINITTEST/$USERADDEDINIT1
    runcmd initiator delete $EXISTINGINITTEST/$USERADDEDINIT2
    runcmd initiator delete $EXISTINGINITTEST/$EXISTINGINIT4
    runcmd hosts delete $EXISTINGINITTEST

    echoit "Existing Initiators to User Added Initiators Test Passed"
}
# Vplex Consistent HLU related tests.   
consistent_hlu_test(){
    test_VPLEX_ORCH_4;
    test_VPLEX_ORCH_5;
    test_VPLEX_ORCH_6;
}

# Consistent HLU Tests
# 1. Export a volume to cluster. Result: HLU should be 1.
# 2. Add another volume to existing cluster Export with user specified HLU. 
#       Result: HLU for the newly added volume should be user choice.
# 3. Add one more volume to the cluster export. 
#       Result: All hosts in the cluster sees the volume with same HLU (least unused number among all its hosts views)
# 4. Remove one volume from cluster export.
# 5. Remove host from Cluster. 
#       Result: StorageView should be deleted on array.
# 6. Add the host again to cluster. 
#       Result: New storageview for the added host should be created on array and all the volumes available in EG 
#               should be added into Storage vew with the same HLU.
# 7. Remove one volume from cluster export and use the volume to create new export group for one of the Host.
#       Result: Newly added host's storageView should have one additional volume that the other host.
# 8. Export a new volume to cluster export and verify the HLU.
# 9. Delete Host's export group.
#       Result: Exclusing volume should be removed from the Host's storageView.
# 10. Delete Cluster's Export Group.
#       Result: All the StorageViews should be deleted on array.


test_VPLEX_ORCH_4(){
    echot "Test VPLEX_ORCH_4: Consistent HLU Validation."
    expname=${EXPORT_GROUP_NAME}tvo4

    # Make sure we start clean; no masking view on the array
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}1 ${HOST2} gone

    set_validation_check false

    # Create the cluster export and masks with a volume
    runcmd export_group create $PROJECT ${expname}1 $NH --type Cluster --volspec "${PROJECT}/${VOLNAME}-1" --clusters "${TENANT}/${CLUSTER}"

    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} 2 1 1

    runcmd export_group update ${PROJECT}/${expname}1 --addVols "${PROJECT}/${VOLNAME}-2+88"

    verify_export ${expname}1 ${HOST1} 2 2 1,88
    verify_export ${expname}1 ${HOST2} 2 2 1,88

    runcmd export_group update ${PROJECT}/${expname}1 --addVols "${PROJECT}/${VOLNAME}-3"
    
    verify_export ${expname}1 ${HOST1} 2 3 1,2,88
    verify_export ${expname}1 ${HOST2} 2 3 1,2,88

    runcmd export_group update ${PROJECT}/${expname}1 --remVols "${PROJECT}/${VOLNAME}-3"
    
    verify_export ${expname}1 ${HOST1} 2 2 1,88
    verify_export ${expname}1 ${HOST2} 2 2 1,88
    
    
    runcmd export_group update ${PROJECT}/${expname}1 --remHosts "${HOST2}"
    
    verify_export ${expname}1 ${HOST1} 2 2 1,88
    verify_export ${expname}1 ${HOST2} gone
    
    runcmd export_group update ${PROJECT}/${expname}1 --addHosts "${HOST2}"
    
    verify_export ${expname}1 ${HOST1} 2 2 1,88
    verify_export ${expname}1 ${HOST2} 2 2 1,88
    
    runcmd export_group update ${PROJECT}/${expname}1 --remVols "${PROJECT}/${VOLNAME}-1"
    
    verify_export ${expname}1 ${HOST1} 2 1 88
    verify_export ${expname}1 ${HOST2} 2 1 88
    
    runcmd export_group create $PROJECT ${expname}2 $NH --type Host --volspec ${PROJECT}/${VOLNAME}-3 --hosts "${HOST2}"
    
    verify_export ${expname}1 ${HOST1} 2 1 88
    verify_export ${expname}1 ${HOST2} 2 2 0,88
    
    runcmd export_group update ${PROJECT}/${expname}1 --addVols "${PROJECT}/${VOLNAME}-1"
    
    verify_export ${expname}1 ${HOST1} 2 2 1,88
    verify_export ${expname}1 ${HOST2} 2 3 0,1,88
    
    # Delete the export group
    runcmd export_group delete $PROJECT/${expname}2
    
    verify_export ${expname}1 ${HOST1} 2 2 1,88
    verify_export ${expname}1 ${HOST2} 2 2 1,88
    
    
    # Verify the zone names, as we know them, are on the switch
    load_zones ${HOST1} 
    verify_zones ${FC_ZONE_A:7} exists

    set_validation_check false

    # Delete the export group
    runcmd export_group delete $PROJECT/${expname}1
    
    # Make sure the mask is gone
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}1 ${HOST2} gone

    verify_no_zones ${FC_ZONE_A:7} ${HOST1}

    set_validation_check true
}
# Test case for consistent lun violation whileing adding host into cluster.
# 1. Export a volume V1 to cluster C1.
# 2. Remove one host H2 from cluster.
# 3. Export a volume V2 to Host H2 with HLU 1. 
# 4. Add H2 into cluster C1. Result: Export Group update should fail as there is an consistent lun violation here.
test_VPLEX_ORCH_5(){
    echot "Test VPLEX_ORCH_5: Consistent HLU Validation."
    expname=${EXPORT_GROUP_NAME}tvo5

    # Make sure we start clean; no masking view on the array
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}1 ${HOST2} gone

    set_validation_check false

    # Create the cluster export and masks with a volume
    runcmd export_group create $PROJECT ${expname}1 $NH --type Cluster --volspec "${PROJECT}/${VOLNAME}-1" --clusters "${TENANT}/${CLUSTER}"

    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} 2 1 1
    
    runcmd export_group update ${PROJECT}/${expname}1 --remHosts "${HOST2}"
    
    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} gone
    
    runcmd export_group create $PROJECT ${expname}2 $NH --type Host --volspec "${PROJECT}/${VOLNAME}-2+1" --hosts "${HOST2}"
    
    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} 2 1 1
    
    #Expecting failure here due to consistent lun violation
    runcmd fail export_group update ${PROJECT}/${expname}1 --addHosts "${HOST2}"
    
    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} 2 1 1
    
    # Delete the export group
    runcmd export_group delete $PROJECT/${expname}2
    runcmd export_group delete $PROJECT/${expname}1
    
    # Make sure the mask is gone
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}1 ${HOST2} gone

    verify_no_zones ${FC_ZONE_A:7} ${HOST1}

    set_validation_check true
}
# Consistent HLU of VPLEX Snaoshot
# 1. Create two vplex snapshot and create vplex volumes from the created snapshots.
# 2. Export Volume V1 to a Cluster. Result: HLU should be 1.
# 3. ExportVolume V2 to one of the Host H2. Result: HLU for the exclusive volume should be 0.
# 4. Export a snapshot1 to a cluster. Result: Snapshot's HLU should be 2 on both the storage views.
# 5. Export a snapshot2 to a host H2. Result: snap2's HLU should be 3 and it should be available only on H2 StorageView.
# 6. Unexport snapshot1 from a cluster.
# 7. Unexport snapshot2 from a host H2.
# 8. Delete vplex volumes created using the snapshots and delete the snapshots. 

test_VPLEX_ORCH_6(){
    echot "Test VPLEX_ORCH_6: Consistent HLU Validation."
    expname=${EXPORT_GROUP_NAME}tvo6
    
    # Make sure we start clean; no masking view on the array
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}1 ${HOST2} gone
    localSnapshot=VPlexLocalSnap
    
    set_validation_check false
    
    runcmd blocksnapshot create ${PROJECT}/${VOLNAME}-1 ${localSnapshot}-1
    runcmd blocksnapshot list ${PROJECT}/${VOLNAME}-1
    # blocksnapshot show ${PROJECT}/${VOLNAME}-1/$localSnapshot-1
    secho "Creating VPLEX volume from snapshot1"
    blocksnapshot expose ${PROJECT}/${VOLNAME}-1/$localSnapshot-1
    
    runcmd blocksnapshot create ${PROJECT}/${VOLNAME}-2 ${localSnapshot}-2
    runcmd blocksnapshot list ${PROJECT}/${VOLNAME}-2
    # blocksnapshot show ${PROJECT}/${VOLNAME}-2/$localSnapshot-2
    secho "Creating VPLEX volume from snapshot2"
    blocksnapshot expose ${PROJECT}/${VOLNAME}-2/$localSnapshot-2
    
    
    # Create the cluster export and masks with a volume
    runcmd export_group create $PROJECT ${expname}1 $NH --type Cluster --volspec "${PROJECT}/${VOLNAME}-1" --clusters "${TENANT}/${CLUSTER}"

    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} 2 1 1
    
    runcmd export_group create $PROJECT ${expname}2 $NH --type Host --volspec ${PROJECT}/${VOLNAME}-2 --hosts "${HOST2}"
    
    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} 2 2 0,1
    
    runcmd export_group update ${PROJECT}/${expname}1 --addVolspec ${PROJECT}/${localSnapshot}-1
    
    verify_export ${expname}1 ${HOST1} 2 2 1,2
    verify_export ${expname}1 ${HOST2} 2 3 0,1,2
    
    runcmd export_group update ${PROJECT}/${expname}2 --addVolspec ${PROJECT}/${localSnapshot}-2
    
    verify_export ${expname}1 ${HOST1} 2 2 1,2
    verify_export ${expname}1 ${HOST2} 2 4 0,1,2,3
    
    runcmd export_group update ${PROJECT}/${expname}1 --remVols "${PROJECT}/${localSnapshot}-1"
    
    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} 2 3 0,1,3
    
    runcmd export_group update ${PROJECT}/${expname}2 --remVols "${PROJECT}/${localSnapshot}-2"
    
    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} 2 2 0,1
    
    
    
    # runcmd export_group update ${PROJECT}/${expname}1 --remHosts "${HOST2}"
    
    # verify_export ${expname}1 ${HOST1} 2 2 1,2
    # verify_export ${expname}1 ${HOST2} 2 2 0,3
    
    
    runcmd export_group delete $PROJECT/${expname}2
    
    verify_export ${expname}1 ${HOST1} 2 1 1
    verify_export ${expname}1 ${HOST2} 2 1 1
    
    runcmd export_group delete $PROJECT/${expname}1
    
    # Make sure the mask is gone
    verify_export ${expname}1 ${HOST1} gone
    verify_export ${expname}1 ${HOST2} gone
    
    secho "Deleting VPLEX volume built on snapshot1"
    run volume delete $PROJECT/$localSnapshot-1 --wait

    secho "Deleting snapshot"
    run blocksnapshot delete $PROJECT/${VOLNAME}-1/$localSnapshot-1
    
    secho "Deleting VPLEX volume built on snapshot2"
    run volume delete $PROJECT/$localSnapshot-2 --wait

    secho "Deleting snapshot"
    run blocksnapshot delete $PROJECT/${VOLNAME}-2/$localSnapshot-2
    
    verify_no_zones ${FC_ZONE_A:7} ${HOST1}

    set_validation_check true
}
