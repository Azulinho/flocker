# Copyright Hybrid Logic Ltd.  See LICENSE file for details.

"""
Functional tests for ``flocker.node.agents.ebs`` using an EC2 cluster.

"""

from uuid import uuid4

from bitmath import Byte

from ..ebs import EBSBlockDeviceAPI
from ..testtools import ec2_client_from_environment
from ....testtools import skip_except
from ..test.test_blockdevice import make_iblockdeviceapi_tests
from ..test.test_blockdevice import REALISTIC_BLOCKDEVICE_SIZE


def ebsblockdeviceapi_for_test(test_case, cluster_id):
    return EBSBlockDeviceAPI(
        ec2_client=ec2_client_from_environment(),
        cluster_id=cluster_id
    )


# ``EBSBlockDeviceAPI`` only implements the ``create`` and ``list`` parts of
# ``IBlockDeviceAPI``. Skip the rest of the tests for now.
@skip_except(
    supported_tests=[
        'test_interface',
        'test_created_is_listed',
        'test_created_volume_attributes',
        'test_list_volume_empty',
        'test_listed_volume_attributes',
        'test_foreign_cluster_volume',
        'test_foreign_volume',
        'test_destroy_unknown_volume',
        'test_destroy_volume',
        'test_destroy_destroyed_volume',
    ]
)
class EBSBlockDeviceAPIInterfaceTests(
        make_iblockdeviceapi_tests(
            blockdevice_api_factory=(
                lambda test_case: ebsblockdeviceapi_for_test(
                    test_case=test_case,
                    cluster_id=uuid4()
                )
            )
        )
):

    """
    Interface adherence Tests for ``EBSBlockDeviceAPI``.
    """
    def test_foreign_cluster_volume(self):
        block_device_api2 = ebsblockdeviceapi_for_test(
            test_case=self,
            cluster_id=uuid4(),
        )

        self.assertVolumesDistinct(block_device_api2)

    def test_foreign_volume(self):
        """
        Non-Flocker Volumes are not listed.
        """
        ec2_client = ec2_client_from_environment()
        requested_volume = ec2_client.connection.create_volume(
            int(Byte(REALISTIC_BLOCKDEVICE_SIZE).to_GB().value),
            ec2_client.zone)

        self.api._wait_for_volume(requested_volume)

        self.assertEqual(self.api.list_volumes(), [])

        ec2_client.connection.delete_volume(requested_volume.id)