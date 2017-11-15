/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.account;

import com.github.intellectualsites.kvantum.api.account.roles.AccountRole;
import com.github.intellectualsites.kvantum.api.account.roles.defaults.Administrator;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.session.ISession;
import com.github.intellectualsites.kvantum.api.util.ApplicationStructure;
import com.github.intellectualsites.kvantum.api.util.SearchResultProvider;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages {@link IAccount} and depends on {@link ApplicationStructure}
 * <p>
 * The global implementation can be retrieved using
 * {@link Kvantum#getApplicationStructure()} then {@link ApplicationStructure#getAccountManager()}
 * </p>
 */
public interface IAccountManager extends SearchResultProvider<IAccount>
{

    String SESSION_ACCOUNT_CONSTANT = "__user_id__";
    Map<String, AccountRole> ROLE_MAP = new ConcurrentHashMap<>();

    /**
     * Check if a given password matches the real password
     *
     * @param candidate Candidate password
     * @param password  Real password
     * @return true if the passwords are matching
     */
    static boolean checkPassword(final String candidate, final String password)
    {
        return BCrypt.checkpw( candidate, password );
    }

    /**
     * Get the container {@link ApplicationStructure}
     * @return {@link ApplicationStructure} implemenation
     */
    ApplicationStructure getApplicationStructure();

    /**
     * Setup the account manager
     * @throws Exception if anything goes wrong
     */
    void setup() throws Exception;

    /**
     * Create an {@link IAccount}
     * @param username Account username
     * @param password Account password
     * @return {@link Optional} containing the account if it was created successfully,
     *                          otherwise an empty optional ({@link Optional#empty()} is returned.
     */
    Optional<IAccount> createAccount(String username, String password);

    /**
     * Get an {@link IAccount} by username.
     * @param username Account username
     * @return {@link Optional} containing the account if it exsists
     *                          otherwise an empty optional ({@link Optional#empty()} is returned.
     */
    Optional<IAccount> getAccount(String username);

    /**
     * Get an {@link IAccount} by ID.
     * @param accountId Account ID
     * @return {@link Optional} containing the account if it exsists
     *                          otherwise an empty optional ({@link Optional#empty()} is returned.
     */
    Optional<IAccount> getAccount(int accountId);

    /**
     * Get an {@link IAccount} by session.
     * @param session session.
     * @return {@link Optional} containing the account if it exsists
     *                          otherwise an empty optional ({@link Optional#empty()} is returned.
     */
    default Optional<IAccount> getAccount(final ISession session)
    {
        if ( !session.contains( SESSION_ACCOUNT_CONSTANT ) )
        {
            return Optional.empty();
        }
        return getAccount( (int) session.get( SESSION_ACCOUNT_CONSTANT ) );
    }

    /**
     * Bind an {@link IAccount} to a {@link ISession}
     * @param account Account
     * @param session Session
     */
    default void bindAccount(final IAccount account, final ISession session)
    {
        session.set( SESSION_ACCOUNT_CONSTANT, account.getId() );
    }

    /**
     * Unbind any account from a {@link ISession}
     * @param session Session to be unbound
     */
    default void unbindAccount(final ISession session)
    {
        session.set( SESSION_ACCOUNT_CONSTANT, null );
    }

    /**
     * Set the data for an account
     * @param account Account
     * @param key Data key
     * @param value Data value
     */
    void setData(IAccount account, String key, String value);

    /**
     * Remove a data value from an account
     * @param account Account
     * @param key Data key
     */
    void removeData(IAccount account, String key);

    /**
     * Load the data into a {@link IAccount}
     * @param account Account to be loaded
     */
    void loadData(IAccount account);

    /**
     * Check if the admin account is created, otherwise a new admin account will be created
     * with credentials:
     * <ul>
     * <li><b>Username:</b> admin</li>
     * <li><b>Password:</b> admin</li>
     * </ul>
     */
    default void checkAdmin()
    {
        final Optional<AccountRole> administratorAccountRole = getAccountRole( Administrator.ADMIN_IDENTIFIER );
        if ( !administratorAccountRole.isPresent() )
        {
            registerAccountRole( Administrator.instance );
        }
        if ( !getAccount( "admin" ).isPresent() )
        {
            Optional<IAccount> adminAccount = createAccount( "admin", "admin" );
            if ( !adminAccount.isPresent() )
            {
                Message.ACCOUNT_ADMIN_FAILED.log();
            } else
            {
                Message.ACCOUNT_ADMIN_CREATED.log( "admin" );
                adminAccount.get().setData( "administrator", "true" );
                adminAccount.get().addRole( Administrator.instance );
            }
        }
    }

    /**
     * Search for an account in the account database by
     * comparing either user ID or username (or both)
     * @param searchQuery Query containing the information
     *                    that will be compared
     * @return Optional
     */
    default Optional<IAccount> searchForAccount(final IAccount searchQuery)
    {
        boolean searchId = searchQuery.getId() != -1;
        boolean searchUsername = searchQuery.getUsername() != null &&
                searchQuery.getUsername() != null &&
                !searchQuery.getUsername().isEmpty() &&
                !"null".equals( searchQuery.getUsername() );
        if ( searchId )
        {
            final Optional<IAccount> returnOptional = getAccount( searchQuery.getId() );
            if ( returnOptional.isPresent() )
            {
                return returnOptional;
            }
        }
        if ( searchUsername )
        {
            final Optional<IAccount> returnOptional = getAccount( searchQuery.getUsername() );
            if ( returnOptional.isPresent() )
            {
                return returnOptional;
            }
        }
        return Optional.empty();
    }

    @Override
    default Collection<? extends IAccount> getResults(final IAccount query)
    {
        final Optional<IAccount> accountOptional = searchForAccount( query );
        return accountOptional.<Collection<? extends IAccount>>map( Collections::singletonList ).orElseGet( Collections::emptyList );
    }

    /**
     * Register an account role
     * @param role Role instance
     */
    default void registerAccountRole(final AccountRole role)
    {
        this.ROLE_MAP.put( role.getRoleIdentifier(), role );
    }

    /**
     * Get all account roles that are registered in the manager
     * @return registered account roles
     */
    default Collection<AccountRole> getRegisteredAccountRoles()
    {
        return this.ROLE_MAP.values();
    }

    /**
     * Try to retrieve an account role based on its identifier
     * @param roleIdentifier Role identifier ({@link AccountRole#getRoleIdentifier()})
     * @return Optional
     */
    default Optional<AccountRole> getAccountRole(final String roleIdentifier)
    {
        if ( this.ROLE_MAP.containsKey( roleIdentifier ) )
        {
            return Optional.of( this.ROLE_MAP.get( roleIdentifier ) );
        }
        return Optional.empty();
    }

}