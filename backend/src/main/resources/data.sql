INSERT INTO registration_invitations (token, role, email, first_name, last_name, expires_at, used_at)
SELECT 'instructor-invite-demo', 'INSTRUCTOR', 'instructor1@tcu.edu', 'Morgan', 'Lee', '2027-12-31 23:59:59', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM registration_invitations WHERE token = 'instructor-invite-demo'
);
