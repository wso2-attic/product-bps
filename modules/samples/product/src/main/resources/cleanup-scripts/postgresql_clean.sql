CREATE OR REPLACE FUNCTION cleanInstances(bpel_inst_state INT, lastActive timestamp)
RETURNS void AS $$
DECLARE
	i bigint;
	inst_cus CURSOR FOR SELECT ID FROM ODE_PROCESS_INSTANCE WHERE INSTANCE_STATE = bpel_inst_state AND LAST_ACTIVE_TIME < lastActive;
BEGIN
	OPEN inst_cus;

	LOOP
		FETCH inst_cus INTO i;
		--If fetch results is not found, exit.
		IF not found THEN
			EXIT;
		END IF;

		DELETE FROM ODE_EVENT WHERE INSTANCE_ID = i;
		DELETE FROM ODE_CORSET_PROP WHERE CORRSET_ID IN (SELECT cs.CORRELATION_SET_ID FROM ODE_CORRELATION_SET cs WHERE cs.SCOPE_ID IN (SELECT os.SCOPE_ID FROM ODE_SCOPE os WHERE	 os.PROCESS_INSTANCE_ID = i));
		DELETE FROM ODE_CORRELATION_SET WHERE SCOPE_ID IN (SELECT os.SCOPE_ID FROM ODE_SCOPE os WHERE os.PROCESS_INSTANCE_ID = i);
		DELETE FROM ODE_PARTNER_LINK WHERE SCOPE_ID IN (SELECT os.SCOPE_ID FROM ODE_SCOPE os WHERE os.PROCESS_INSTANCE_ID = i);
		DELETE FROM ODE_XML_DATA_PROP WHERE XML_DATA_ID IN (SELECT xd.XML_DATA_ID FROM ODE_XML_DATA xd WHERE xd.SCOPE_ID IN (SELECT os.SCOPE_ID FROM ODE_SCOPE os WHERE os.PROCESS_INSTANCE_ID = i));
		DELETE FROM ODE_XML_DATA WHERE SCOPE_ID IN (SELECT os.SCOPE_ID FROM ODE_SCOPE os WHERE os.PROCESS_INSTANCE_ID = i);
		DELETE FROM ODE_SCOPE WHERE PROCESS_INSTANCE_ID = i;
		DELETE FROM ODE_MEX_PROP WHERE MEX_ID IN (SELECT mex.MESSAGE_EXCHANGE_ID FROM ODE_MESSAGE_EXCHANGE mex WHERE mex.PROCESS_INSTANCE_ID = i);
		DELETE FROM ODE_MESSAGE WHERE MESSAGE_EXCHANGE_ID IN (SELECT mex.MESSAGE_EXCHANGE_ID FROM ODE_MESSAGE_EXCHANGE mex WHERE mex.PROCESS_INSTANCE_ID = i);
		DELETE FROM ODE_MESSAGE_EXCHANGE WHERE PROCESS_INSTANCE_ID = i;
		DELETE FROM ODE_MESSAGE_ROUTE WHERE PROCESS_INSTANCE_ID = i;
		DELETE FROM ODE_PROCESS_INSTANCE WHERE ID = i;

	END LOOP;

	CLOSE inst_cus;
END;
$$
LANGUAGE plpgsql;

BEGIN;
--default instance state to delete - 30
--time interval to start deletion - 48 hours
SELECT cleanInstances(30,localtimestamp - interval '48 hours');
COMMIT;