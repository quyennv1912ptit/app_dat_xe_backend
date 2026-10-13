-- Apply once to the existing PostgreSQL schema before starting the renamed application.
-- Execute with the same search_path/schema used by Hibernate. No rows are deleted.
-- If old and new tables coexist, inspect them first; this script deliberately fails.
BEGIN;

ALTER TABLE bookings RENAME TO rides;
ALTER TABLE booking_status_histories RENAME TO ride_status_histories;
ALTER TABLE ride_status_histories RENAME COLUMN booking_id TO ride_id;
ALTER TABLE vehicle_handover_reports RENAME COLUMN booking_id TO ride_id;
ALTER TABLE vehicle_handover_reports
    RENAME CONSTRAINT uk_handover_booking_phase TO uk_handover_ride_phase;

COMMIT;
