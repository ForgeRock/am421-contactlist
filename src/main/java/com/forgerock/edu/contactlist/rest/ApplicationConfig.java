package com.forgerock.edu.contactlist.rest;

import com.forgerock.edu.contactlist.rest.security.filter.LocalTokenStoreSessionValidatorFilter;
import com.forgerock.edu.contactlist.rest.exception.EntryNotFoundExceptionMapper;
import com.forgerock.edu.contactlist.rest.exception.ConstraintViolationExceptionMapper;
import com.forgerock.edu.contactlist.rest.exception.OptimisticLockExceptionMapper;
import com.forgerock.edu.contactlist.rest.exception.IncorrectPasswordExceptionMapper;
import com.forgerock.edu.contactlist.rest.exception.NotAuthorizedExceptionMapper;
import com.forgerock.edu.contactlist.rest.exception.InvalidTokenIdExceptionMapper;
import com.forgerock.edu.contactlist.rest.exception.ErrorResultIOExceptionMapper;
import javax.ws.rs.ApplicationPath;
import com.forgerock.edu.contactlist.rest.auth.AuthResource;
import com.forgerock.edu.contactlist.rest.security.filter.RolesAllowedDynamicFeature;
import com.forgerock.edu.contactlist.rest.contact.ContactGroupsResource;
import com.forgerock.edu.contactlist.rest.userprofile.UserProfileDisabledCheckResource;
import com.forgerock.edu.contactlist.rest.userprofile.UserProfilesResource;
import com.forgerock.edu.contactlist.rest.exception.ConstraintViolatedExceptionMapper;
import com.forgerock.edu.contactlist.rest.security.filter.LocalLDAPBasedPrivilegeCalculatorFilter;
import com.forgerock.edu.contactlist.rest.security.filter.OpenAMPolicyEnforcementFilter;
import com.forgerock.edu.contactlist.rest.security.filter.OpenAMSessionValidatorFilter;
import com.forgerock.edu.contactlist.rest.security.filter.OpenIDConnectSessionValidatorFilter;
import com.forgerock.edu.contactlist.rest.security.filter.ResourceOwnerCalculatorFilter;
import com.forgerock.edu.contactlist.rest.security.filter.SelectedRoleBasedPrivilegeCalculatorFilter;
import com.forgerock.edu.contactlist.rest.userprofile.ActiveUserProfilesResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * REST configuration class. Registers all the resources, filters and exception
 * mapper classes.
 *
 * @author vrg
 */
@ApplicationPath("rest")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        super(
                //TODO Ch4L1Ex3: Enable the OpenAMSessionValidatorFilter and the LocalLDAPBasedPrivilegeCalculatorFilter
                //TODO Ch4L2Ex2: Disable the LocalLDAPBasedPrivilegeCalculatorFilter and enable the SelectedRoleBasedPrivilegeCalculatorFilter
                //TODO Ch4L4Ex2: Disable the SelectedRoleBasedPrivilegeCalculatorFilter, disable the RolesAllowedDynamicFeature and enable the OpenAMPolicyEnforcementFilter
                //TODO Ch5L1Ex4: Disable the OpenAMSessionValidatorFilter, disable the OpenAMPolicyEnforcementFilter, enable the OpenIDConnectSessionValidatorFilter and re-enable the RolesAllowedDynamicFeature

                ////////////////////////////////////////////////
                // Session Validation Filters - these are responsible for creating the SecurityContext for a single request
                LocalTokenStoreSessionValidatorFilter.class, 
                //OpenAMSessionValidatorFilter.class,
                //OpenIDConnectSessionValidatorFilter.class,

                ////////////////////////////////////////////////
                // SecurityContext Modifier Filters
                //LocalLDAPBasedPrivilegeCalculatorFilter.class,
                //SelectedRoleBasedPrivilegeCalculatorFilter.class,
                ResourceOwnerCalculatorFilter.class,

                ////////////////////////////////////////////////
                // Authorization Filters
                // OpenAMPolicyEnforcementFilter.class,
                
                ////////////////////////////////////////////////
                // Resources
                AuthResource.class,
                UserProfileDisabledCheckResource.class,
                ContactGroupsResource.class,
                ActiveUserProfilesResource.class,
                UserProfilesResource.class,
                ////////////////////////////////////////////////
                // Exception mappers
                EntryNotFoundExceptionMapper.class,
                ConstraintViolatedExceptionMapper.class,
                ConstraintViolationExceptionMapper.class,
                ErrorResultIOExceptionMapper.class,
                OptimisticLockExceptionMapper.class,
                NotAuthorizedExceptionMapper.class,
                IncorrectPasswordExceptionMapper.class,
                InvalidTokenIdExceptionMapper.class
        );
        ////////////////////////////////////////////////////////
        //       Features
        this
                .register(RolesAllowedDynamicFeature.class)
                .register(JsonProcessingFeature.class)
                .register(JacksonFeature.class);
    }
}
